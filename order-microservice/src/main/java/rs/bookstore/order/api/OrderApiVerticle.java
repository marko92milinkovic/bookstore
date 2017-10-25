/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.serviceproxy.ProxyHelper;
import rs.bookstore.constants.PortsConstants;
import rs.bookstore.lib.RxMicroServiceVerticle;
import rs.bookstore.order.CheckoutResult;
import rs.bookstore.order.Order;
import rs.bookstore.order.impl.OrderServiceImpl;
import rs.bookstore.order.service.OrderService;
import rx.Observable;
import rx.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static rs.bookstore.order.service.OrderService.SERVICE_ADDRESS;
import static rs.bookstore.order.service.OrderService.SERVICE_NAME;

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
        ProxyHelper.registerService(OrderService.class, vertx.getDelegate(), orderService, SERVICE_ADDRESS);

        int port = config().getInteger("http.port", PortsConstants.ORDERS_SERVICE_PORT);
        String host = config().getString("host", "localhost");
        Router router = Router.router(vertx);

        router.get("/getall/:customerId")
                .handler(rc -> {
                    orderService.retrieveOrdersForCustomer(Long.parseLong(rc.pathParam("customerId")),
                            RxHelper.toFuture(
                                    //ok response
                                    list -> rc.response().end(Json.encodePrettily(list)),
                                    //not ok response
                                    cause -> rc.response().end(cause.getMessage())));
                });

        Single<HttpServer> server = vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(port, host);

        Single<Void> publishEventBusService = publishEventBusService(SERVICE_NAME, SERVICE_ADDRESS, OrderService.class);

        vertx.eventBus().consumer(ORDER_EVENT_ADDRESS, message -> {
            System.out.println("Stigla narudzbina sa zadate adrese: " + message.body());
            message.reply(new CheckoutResult().setOrder(new Order()).setResultMessage("cart checkouted").toJson());
        });

        Single.concat(server, publishEventBusService, vertx.rxDeployVerticle(TestVerticle.class.getName()))
                .subscribe(next->{
                    System.out.println("Proslo: "+next);
                }, startFuture::fail, startFuture::complete);
    }

    private void handleError(RoutingContext rc, Throwable error) {
        rc.response().setStatusCode(500).end(error.getMessage());
    }

    private <T, R> void next(AsyncResult<T> asyncResult, Function<T, R> next) throws Throwable {
        if (asyncResult.succeeded()) {
            next.apply(asyncResult.result());
        } else {
            throw asyncResult.cause();
        }
    }


    String ORDER_EVENT_ADDRESS = "events.service.shopping.to.order";

//    private Single<Void> prepareDispatcher() {
//        return vertx.rxDeployVerticle("OrderDispatcher",
//                new DeploymentOptions().setConfig(config()))
//                .map(stringRes -> (Void) null);
//    }
}
