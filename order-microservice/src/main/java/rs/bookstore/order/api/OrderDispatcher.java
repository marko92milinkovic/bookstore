/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.api;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.MessageSource;
import java.util.List;
import rs.bookstore.lib.MicroServiceVerticle;
import rs.bookstore.lib.RxMicroServiceVerticle;
import rs.bookstore.order.Order;
import rs.bookstore.order.impl.OrderServiceImpl;
import rs.bookstore.order.service.OrderService;

/**
 *
 * @author marko
 */
public class OrderDispatcher extends RxMicroServiceVerticle {

    private final OrderService orderService = new OrderServiceImpl(vertx, config());

    @Override
    public void start(Future<Void> future) throws Exception {
        super.start();
        //Rxify this!!
//        MessageSource.<JsonObject>getConsumer(discovery,
//                new JsonObject().put("name", "*place name*"),
//                ar -> {
//                    if (ar.succeeded()) {
//                        MessageConsumer<JsonObject> orderConsumer = ar.result();
//                        orderConsumer.handler(message -> {
//                            Order wrappedOrder = wrapRawOrder(message.body());
//                            dispatchOrder(wrappedOrder, message);
//                        });
//                        future.complete();
//                    } else {
//                        future.fail(ar.cause());
//                    }
//                });
    }

    private Order wrapRawOrder(JsonObject orderAsJson) {
        return new Order(orderAsJson);
    }

    private void dispatchOrder(Order order, Message<JsonObject> sender) {
        Future<Void> orderCreateFuture = Future.future();
        orderService.createOrder(order, orderCreateFuture.completer());
        orderCreateFuture
                .compose(orderCreated -> applyInventoryChanges(order))
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        sender.reply("ok");
                    } else {
                        sender.fail(5000, ar.cause().getMessage());
                        ar.cause().printStackTrace();
                    }
                });
    }

    private Future<Void> applyInventoryChanges(Order order) {
        Future<Void> future = Future.future();
        // get REST endpoint
        Future<HttpClient> clientFuture = Future.future();
//        HttpEndpoint.getClient(discovery,
//                new JsonObject().put("name", "inventory-service"),
//                clientFuture.completer());
        // modify the inventory changes via REST API
        return future;
    }
}
