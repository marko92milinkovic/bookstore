/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.customer.api;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import rs.bookstore.customer.service.CustomerService;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author marko
 */
public class CustomerVerticle extends MicroServiceVerticle {

    @Override
    public void start(Future<Void> future) throws Exception {

        CustomerService service = CustomerService.createProxy(vertx);

        vertx.deployVerticle("/home/marko/NetBeansProjects/bookstore/customer-microservice/src/main/resources/service.js", ar -> {
            if (ar.succeeded()) {
                System.out.println("postavio js verticle");
                future.complete();
            } else {
                ar.cause().printStackTrace();
                future.fail(ar.cause());
            }
        });

        Router router = Router.router(vertx);

        router.get("/js").handler(rc -> {
            String username = rc.request().getParam("user");
            if (username == null || username.trim().equals("")) {
                rc.response().end("Not found");
                return;
            }
            service.getCustomerByUsername(username, ar -> {
                if (ar.succeeded()) {
                    System.out.println("Uspeo sam");
                    System.out.println("Rezultat servisa: " + ar.result());
                    rc.response().end(ar.result().toJson().encodePrettily());
                } else {
                    System.out.println("Nije dobro");
                    ar.cause().printStackTrace();
                    rc.response().end(ar.cause().getMessage());
                }
            });
        });

        publishEventBusService(CustomerService.SERVICE_NAME, CustomerService.SERVICE_ADDRESS, CustomerService.class, ar -> {
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
