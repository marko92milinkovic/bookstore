/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.customer.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author marko
 */
@VertxGen
@ProxyGen
public interface CustomerService {


  /**
   * The address on which the service is published.
   */
  String SERVICE_ADDRESS = "service.customer.proxy";
    
    /**
     * Method called to create a proxy (to consume the service).
     *
     * @param vertx vert.x
     * @return the proxy on the {@link CustomerService}
     */
    static CustomerService createProxy(Vertx vertx) {
        return ProxyHelper.createProxy(CustomerService.class, vertx, SERVICE_ADDRESS);
    }

    /**
     * Method called to create a proxy (to consume the service).
     *
     * @param vertx vert.x
     * @param service
     */
    static void registerService(Vertx vertx, CustomerService service) {
        ProxyHelper.registerService(CustomerService.class, vertx, service, SERVICE_ADDRESS);
    }

    void getCustomerByUsername(String username, Handler <AsyncResult <Customer>> resultHandler);

    void createNewCustomer(Customer customer, Handler <AsyncResult <Customer>> resultHandler);





    void hi(String text, Handler <AsyncResult <String>> resultHandler);
}
