/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import rs.bookstore.lib.RxMicroServiceVerticle;
import rs.bookstore.order.impl.OrderServiceImpl;
import rs.bookstore.order.service.OrderService;
import static rs.bookstore.order.service.OrderService.SERVICE_ADDRESS;
import static rs.bookstore.order.service.OrderService.SERVICE_NAME;
import rx.Single;

/**
 *
 * @author marko
 */
public class OrderApiVerticle extends RxMicroServiceVerticle {

    private OrderService orderService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(); //To change body of generated methods, choose Tools | Templates.

        this.orderService = new OrderServiceImpl(vertx, config());
//        ProxyHelper.registerService(OrderService.class, vertx, orderService, SERVICE_ADDRESS);
        publishEventBusService(SERVICE_NAME, SERVICE_ADDRESS, OrderService.class)
                .concatWith(prepareDispatcher())
                .subscribe(startFuture::complete, startFuture::fail, () -> System.out.println("Order and dispatch verticles prepared"));
    }

    private Single<Void> prepareDispatcher() {
        return vertx.rxDeployVerticle("OrderDispatcher",
                new DeploymentOptions().setConfig(config()))
                .map(stringRes -> (Void) null);
    }

}
