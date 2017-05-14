/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.serviceproxy.ProxyHelper;
import rs.bookstore.constants.PortsConstants;
import rs.bookstore.lib.RxMicroServiceVerticle;
import rs.bookstore.order.CheckoutResult;
import rs.bookstore.order.Order;
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
        ProxyHelper.registerService(OrderService.class, vertx.getDelegate(), orderService, SERVICE_ADDRESS);
        publishEventBusService(SERVICE_NAME, SERVICE_ADDRESS, OrderService.class)
//                .concatWith(prepareDispatcher())
                .subscribe(startFuture::complete, startFuture::fail);

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

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(port, host)
                .subscribe(server -> System.out.println("Order server deployed"), System.err::println);
        
        vertx.eventBus().consumer(ORDER_EVENT_ADDRESS, message->{
            System.out.println("Stigla korpa sa zadate adrese: "+message.body());
            message.reply(new CheckoutResult().setOrder(new Order()).setResultMessage("cart checkouted").toJson());
        });

    }
    
        String ORDER_EVENT_ADDRESS = "events.service.shopping.to.order";


//    private Single<Void> prepareDispatcher() {
//        return vertx.rxDeployVerticle("OrderDispatcher",
//                new DeploymentOptions().setConfig(config()))
//                .map(stringRes -> (Void) null);
//    }

}
