/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.apigateway;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
//import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import rs.bookstore.customer.service.Customer;
import rs.bookstore.customer.service.CustomerService;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author markom
 */
public class HttpServerVerticle extends MicroServiceVerticle {
    
    MongoAuth authProvider;
    //temporary storage
    List<JsonObject> reviews = new ArrayList<>();
    
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
        router.route("/private/*").handler(RedirectAuthHandler.create(authProvider, "/view/login.html"));
        // Any requests to URI starting '/api/' require login
//        ---------------------------OTKOMENTARISATI---------------
        router.route("/auth/*").handler(RedirectAuthHandler.create(authProvider, "/view/login.html"));

        // book api dispatcher
        router.route("/api/bookservice/*").handler(this::dispatchBookRequests);

        // reviews
        router.route("/api/reviews/*").handler(this::dispatchReviewRequest);
        router.route("/eventbus/reviews/*").handler(eventbusReviewsHandler());

        // Serve the static private pages from directory 'private'
        router.route("/private/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("private"));

        // Handles the actual login
        router.post("/loginhandler").handler(FormLoginHandler.create(authProvider)
                .setDirectLoggedInOKURL("/")
                .setReturnURLParam("/loginFailed"));
        router.route("/auth/customer/get").handler(this::getCustomerCredentials);

        // Implement logout
        router.route("/logout").handler(context -> {
            context.clearUser();
            // Redirect back to the index page
            context.response().putHeader("location", "/").setStatusCode(302).end();
        });

        // Serve the non private static pages
        router.route().handler(StaticHandler.create());
        
        vertx.createHttpServer().requestHandler(router::accept).listen(8080, ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }
    
    private Future<List<Record>> getHttpMicroservices() {
        Future<List<Record>> recordsFuture = Future.future();
        discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE),
                recordsFuture);
        return recordsFuture;
    }
    
    private void dispatchBookRequests(RoutingContext rc) {
        
        circuitBreaker.execute(cbFuture -> {
            discovery.getRecord(record
                    -> record.getType().equals(HttpEndpoint.TYPE)
                    && record.getName().equals("book.microservice"), ar -> {
                
                if (ar.succeeded() && ar.result() != null) {
                    
                    Record bookMicroserviceRecord = ar.result();
                    HttpClient httpClient
                            = discovery.getReference(bookMicroserviceRecord).get();
                    
                    String path = rc.request().uri();
                    
                    String apiPAth = (path.split("/api/bookservice"))[1];
                    
                    HttpClientRequest get = httpClient.request(rc.request().method(), apiPAth, booksResponse -> {
                        booksResponse.bodyHandler(body -> {
                            if (booksResponse.statusCode() >= 500) {
                            } else {
                                HttpServerResponse toRsp = rc.response()
                                        .setStatusCode(booksResponse.statusCode());
                                booksResponse.headers().forEach(header -> {
                                    toRsp.putHeader(header.getKey(), header.getValue());
                                });
                                toRsp.end(body);
                                cbFuture.complete();
                            }
                        });
                    });
                    System.out.println("Call book-microservice on url: " + get.uri());
                    get.end();
                } else {
                    rc.response().end("Http microservices not found");
                    cbFuture.complete();
                    if (ar.cause() != null) {
                        ar.cause().printStackTrace();
                    }
                }
            }
            );
        }).setHandler(ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
                rc.response()
                        .setStatusCode(502)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", "bad_gateway")
                                //.put("message", ex.getMessage())
                                .encodePrettily());
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
            }).setHandler(ar-> {
                if (ar.succeeded()) {
                Customer res = ar.result();
                if (res == null) {
                    rc.response().end("NOT found....see line 137");
                } else {
                    rc.response()
                            .putHeader("content-type", "application/json")
                            .end(res.toJson().encodePrettily());
                }
            } else {
                rc.response().end(ar.cause().getMessage());
                ar.cause().printStackTrace();
            }
            });
        }
        
    }
    
    private void dispatchReviewRequest(RoutingContext rc) {
        User user = rc.user();

//        if(user==null) {
//            rc.response().setStatusCode(401).end("not logged in");
//            return;
//        }
        int creatorId = user.principal().getInteger("_id");
        String comment = rc.request().getParam("comment");
        int rate = Integer.parseInt(rc.request().getParam("rate"));
        int bookId = Integer.parseInt(rc.request().getParam("bookId"));

        //send comment to review-service
        JsonObject review = new JsonObject()
                .put("comment", comment)
                .put("rate", rate)
                .put("bookId", bookId)
                .put("creatorId", creatorId);
        reviews.add(review);
        
        OptionalDouble avgRateOptional = reviews.stream().mapToInt(r -> r.getInteger("rate")).average();
        double avgRate = rate;
        if (avgRateOptional.isPresent()) {
            avgRate = avgRateOptional.getAsDouble();
        }
        
        rc.vertx().eventBus().publish("book.reviews." + bookId, new JsonObject().put("review", review).put("avgRate", avgRate));
        rc.response().end("ok :) ");
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
    
    private SockJSHandler eventbusReviewsHandler() {
        BridgeOptions bridgeOptions = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("book\\.reviews\\.[0-9]+"));
        
        return SockJSHandler.create(vertx).bridge(bridgeOptions);
    }
}
