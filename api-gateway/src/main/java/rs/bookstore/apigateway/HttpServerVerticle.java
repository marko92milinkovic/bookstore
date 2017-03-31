/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.apigateway;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.servicediscovery.types.EventBusService;
import rs.bookstore.customer.service.Customer;
import rs.bookstore.customer.service.CustomerService;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author markom
 */
public class HttpServerVerticle extends MicroServiceVerticle {

    MongoAuth authProvider;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        String uri = "mongodb://localhost:27017";
        String db = "master";

        JsonObject mongoconfig = new JsonObject()
                .put("connection_string", uri)
                .put("db_name", db);

        MongoClient client = MongoClient.createShared(vertx, mongoconfig);
        JsonObject authProperties = new JsonObject();
        authProvider = MongoAuth.create(client, authProperties);
        authProvider.setCollectionName("user");
        authProvider.setPasswordField("password");
        authProvider.setUsernameField("username");
        authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.NO_SALT);
        authProvider.setPermissionField("permission");

        Router router = Router.router(vertx);

        // We need cookies, sessions and request bodies
        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        // We need a user session handler too to make sure the user is stored in the session between requests
        router.route().handler(UserSessionHandler.create(authProvider));

        // Any requests to URI starting '/private/' require login
        router.route("/private/*").handler(RedirectAuthHandler.create(authProvider, "/loginpage.html"));

        // Serve the static private pages from directory 'private'
        router.route("/private/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("private"));

        // Handles the actual login
        router.post("/loginhandler").handler(FormLoginHandler.create(authProvider));
        router.route("/customer/get").handler(this::getCustomerCredentials);

        // Implement logout
        router.route("/logout").handler(context -> {
            context.clearUser();
            // Redirect back to the index page
            context.response().putHeader("location", "/").setStatusCode(302).end();
        });

        // Serve the non private static pages
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(8500, ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    private void getCustomerCredentials(RoutingContext rc) {
        if (rc.user() == null) {
            rc.response().setStatusCode(401).end("No user authenticated");
            return;
        }

        JsonObject principal = rc.user().principal();
        System.out.println("Principal is: " + principal);
        String username = principal.getString("username");
        Long customer_id = principal.getLong("customer_id");

        if (username == null || customer_id == null) {
            rc.response()
                    .putHeader("content-type", "application/json")
                    .end(new Customer(
                            new JsonObject()
                            .put("username", "testUsername")
                            .put("id", "testId")).toJson().encodePrettily());
        } else {
            Future<CustomerService> futureCustomerService = Future.future();
            EventBusService.getServiceProxyWithJsonFilter(
                    discovery,
                    new JsonObject().put("name", CustomerService.SERVICE_NAME),
                    CustomerService.class,
                    futureCustomerService.completer());
            futureCustomerService.compose(customerService -> {
                Future<Customer> customerFuture = Future.future();
                customerService.getCustomerByUsername(username, customerFuture.completer());
                return customerFuture;
            })
                    .setHandler(resultHandlerNonEmpty(rc));
        }

    }

    //TO DO: refactor
    private <T> Handler<AsyncResult<T>> resultHandlerNonEmpty(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                T res = ar.result();
                if (res == null) {
                    context.response().end("NOT found....see line 137");
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(res.toString());
                }
            } else {
                context.response().end(ar.cause().getMessage());
                ar.cause().printStackTrace();
            }
        };
    }
}
