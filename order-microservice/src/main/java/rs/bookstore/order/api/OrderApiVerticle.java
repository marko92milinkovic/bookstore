/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import static io.vertx.core.Future.future;
import io.vertx.serviceproxy.ProxyHelper;
import rs.bookstore.lib.MicroServiceVerticle;
import rs.bookstore.order.impl.OrderServiceImpl;
import rs.bookstore.order.service.OrderService;
import static rs.bookstore.order.service.OrderService.SERVICE_ADDRESS;
import static rs.bookstore.order.service.OrderService.SERVICE_NAME;

/**
 *
 * @author marko
 */
public class OrderApiVerticle extends MicroServiceVerticle {

    private OrderService orderService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(); //To change body of generated methods, choose Tools | Templates.

        this.orderService = new OrderServiceImpl(vertx, config());
        ProxyHelper.registerService(OrderService.class, vertx, orderService, SERVICE_ADDRESS);
        publishEventBusService(SERVICE_NAME, SERVICE_ADDRESS, OrderService.class, startFuture
                .compose(servicePublished -> prepareDispatcher())
                .setHandler(startFuture.completer()));
    }


    private Future<Void> prepareDispatcher() {
        Future<String> future = Future.future();
        vertx.deployVerticle(new OrderDispatcher(orderService),
                new DeploymentOptions().setConfig(config()),
                future.completer());
        return future.map(r -> null);
    }

}
