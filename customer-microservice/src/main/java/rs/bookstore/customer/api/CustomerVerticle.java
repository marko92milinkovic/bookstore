/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.customer.api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import static rs.bookstore.constants.MicroServiceNamesConstants.CUSTOMER_SERVICE_RPC;

import io.vertx.rx.java.RxHelper;
import rs.bookstore.customer.service.Customer;
import rs.bookstore.customer.service.CustomerService;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author marko
 */
public class CustomerVerticle extends MicroServiceVerticle {

    @Override
    public void start(Future<Void> future) throws Exception {
        super.start();
        CustomerService service = CustomerService.createProxy(vertx);
        System.out.println("CONF: "+config());

        vertx.deployVerticle("service.js", new DeploymentOptions().setConfig(config()), ar -> {
            if (ar.succeeded()) {
                future.complete();
            } else {
                future.fail(ar.cause());
            }
        });
        System.out.println(context.deploymentID());
        Router router = Router.router(vertx);
        Customer customer = new Customer(-1l, "blac", "cas", "Asd", 1236l);
        router.get("/js").handler(rc -> {

            service.hi("Cao", RxHelper.toFuture(
                    c -> rc.response().end("response is: "+c),
                    error-> rc.response().end("error")
            ));
//            service.createNewCustomer(customer, RxHelper.toFuture(
//                    c -> rc.response().end("custoemr is: "+c),
//                    error-> rc.response().end("error")
//            ));
//            System.out.println("Ovo ne reaguje.........?");
//            String username = rc.request().getParam("user");
//            if (username == null || username.trim().equals("")) {
//                rc.response().end("Not found");
//                return;
//            }
//            service.getCustomerByUsername(username, ar -> {
//                if (ar.succeeded()) {
//                    System.out.println("Uspeo sam");
//                    System.out.println("Rezultat servisa: " + ar.result());
//                    rc.response().end(ar.result().toJson().encodePrettily());
//                } else {
//                    System.out.println("Nije dobro");
//                    ar.cause().printStackTrace();
//                    rc.response().end(ar.cause().getMessage());
//                }
//            });
        });

        publishEventBusService(CUSTOMER_SERVICE_RPC, CustomerService.SERVICE_ADDRESS, CustomerService.class, ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Customer service published : " + ar.succeeded());
            }
        });
        

        HttpServer server = vertx.createHttpServer();

        server.requestHandler(router::accept).listen(8600, "localhost");
    }

}
