/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.inventory.api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author markom
 */
public class InventoryAPIVerticle extends MicroServiceVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();

        Router router = Router.router(vertx);
        router.get(API_INCR).handler(this::increase);
        router.get(API_DECR).handler(this::decrease);
        router.get(API_INVENTORY).handler(this::retrieveInventory);

        vertx.createHttpServer().requestHandler(router::accept).listen(9003, "localhost");

        vertx.deployVerticle("src/main/resources/InventoryStorageVerticle.groovy", new DeploymentOptions(config().put("test", "testing")), ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                ar.cause().printStackTrace();
                startFuture.fail(ar.cause());
            }
        });

    }

    private void retrieveInventory(RoutingContext rc) {
        long bookId;
        try {
            bookId = Long.parseLong(rc.pathParam("bookId"));
        } catch (Exception e) {
            rc.response().setStatusCode(400).end("Invalid bookId");
            return;
        }
        vertx.eventBus().send("inventory.storage.balance",
                new JsonObject().put("bookId", bookId),
                ar -> {
                    if (ar.failed()) {
                        rc.response().end(ar.cause().getMessage());
                    } else {
                        rc.response().end(ar.result().body().toString());
                    }
                });
    }

    private void decrease(RoutingContext rc) {
        long bookId;
        int amount;
        try {
            bookId = Long.parseLong(rc.request().getParam("bookId"));
            amount = Integer.parseInt(rc.request().getParam("amount"));
        } catch (Exception e) {
            rc.response().setStatusCode(400).end("Invalid params");
            return;
        }
        vertx.eventBus().send("inventory.storage.decrease",
                new JsonObject().put("bookId", bookId).put("amount", amount),
                ar -> {
                    if (ar.failed()) {
                        rc.response().end(ar.cause().getMessage());
                    } else {
                        rc.response().end(ar.result().body().toString());
                    }
                });
    }

    private void increase(RoutingContext rc) {
        long bookId;
        int amount;
        try {
            bookId = Long.parseLong(rc.request().getParam("bookId"));
            amount = Integer.parseInt(rc.request().getParam("amount"));
        } catch (Exception e) {
            rc.response().setStatusCode(400).end("Invalid params");
            return;
        }

        vertx.eventBus().send("inventory.storage.increase",
                new JsonObject().put("bookId", bookId).put("amount", amount),
                ar -> {
                    if (ar.failed()) {
                        rc.response().end(ar.cause().getMessage());
                    } else {
                        rc.response().end(ar.result().body().toString());
                    }
                });

    }

    private static final String API_INCR = "/increase";
    private static final String API_DECR = "/decrease";
    private static final String API_INVENTORY = "/inventory/:bookId";

}
