/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.inventory.api;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import rs.bookstore.constants.MicroServiceNamesConstants;
import rs.bookstore.constants.PortsConstants;
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

        int port = config().getInteger("http.port", PortsConstants.INVENTORY_SERVICE_HTTP_PORT);
        String host = config().getString("host", "localhost");

        vertx.createHttpServer().requestHandler(router::accept).listen(port, host);

        vertx.deployVerticle("src/main/resources/InventoryStorageVerticle.groovy",
                ar -> {
                    if (ar.succeeded()) {
                        publishHttpEndpoint(MicroServiceNamesConstants.INVENTORY_SERVICE,
                                "localhost", PortsConstants.INVENTORY_SERVICE_HTTP_PORT, startFuture.completer());
                    } else {
                        ar.cause().printStackTrace();
                        startFuture.fail(ar.cause());
                    }
                }
        );
    }

    private void retrieveInventory(RoutingContext rc) {
        System.out.println("Retrieve inventory");
        long bookId;
        try {
            bookId = Long.parseLong(rc.pathParam("bookId"));
        } catch (Exception e) {
            rc.response().setStatusCode(400).end("Invalid bookId");
            return;
        }
        System.out.println("for book: " + bookId);
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
        System.out.println("Decrease inventory");
        long bookId;
        int amount;
        try {
            bookId = Long.parseLong(rc.request().getParam("bookId"));
            amount = Integer.parseInt(rc.request().getParam("amount"));
        } catch (Exception e) {
            rc.response().setStatusCode(400).end("Invalid params");
            return;
        }

        System.out.println("for book: " + bookId);

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
        System.out.println("Increase inventory");
        long bookId;
        int amount;
        try {
            bookId = Long.parseLong(rc.request().getParam("bookId"));
            amount = Integer.parseInt(rc.request().getParam("amount"));
        } catch (Exception e) {
            rc.response().setStatusCode(400).end("Invalid params");
            return;
        }
        System.out.println("for book: " + bookId);

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
