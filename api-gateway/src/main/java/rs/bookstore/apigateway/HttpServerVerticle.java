/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.apigateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

/**
 *
 * @author markom
 */
public class HttpServerVerticle extends AbstractVerticle {

    MongoAuth authProvider;

    @Override
    public void start() throws Exception {
        String uri = "mongodb://localhost:27017";
        String db = "master";

        JsonObject mongoconfig = new JsonObject()
                .put("connection_string", uri)
                .put("db_name", db);

        MongoClient client = MongoClient.createShared(vertx, mongoconfig);
        JsonObject authProperties = new JsonObject();
        authProvider = MongoAuth.create(client, authProperties);
        authProvider.setCollectionName("account");
        authProvider.setPasswordField("password");
        authProvider.setUsernameField("username");
        authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.NO_SALT);
        authProvider.setPermissionField("permission");

        Router router = Router.router(vertx);

        vertx.createHttpServer().requestHandler(router::accept).listen(8500, "localhost");

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

        // Implement logout
        router.route("/logout").handler(context -> {
            context.clearUser();
            // Redirect back to the index page
            context.response().putHeader("location", "/").setStatusCode(302).end();
        });

        // Serve the non private static pages
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(8500);

    }

//    private void logIn(RoutingContext rc) {
//        String username = rc.request().getParam("username");
//        String password = rc.request().getParam("password");
//
//        System.out.println("PARAMS: " + rc.request().params());
//        JsonObject authInfo = new JsonObject()
//                .put("username", username)
//                .put("password", password);
//        authProvider.authenticate(authInfo, res -> {
//            if (res.succeeded()) {
//                User user = res.result();
//                user.setAuthProvider(authProvider);
//                rc.setUser(user);
//                rc.reroute("/private/private_page");
//            } else {
//                // Failed!
//                res.cause().printStackTrace();
//            }
//        });
//    }
}
