/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.List;
import rs.bookstore.order.Order;

/**
 *
 * @author marko
 */
@VertxGen
@ProxyGen
public interface OrderService {

  String SERVICE_NAME = "order-storage-eb-service";
  String SERVICE_ADDRESS = "service.order";

  @Fluent
  OrderService initializePersistence(Handler<AsyncResult<Void>> resultHandler);
  
  @Fluent
  OrderService retrieveOrdersForCustomer(Long customerId, Handler<AsyncResult<List<Order>>> resultHandler);

  @Fluent
  OrderService createOrder(Order order, Handler<AsyncResult<Void>> resultHandler);

  @Fluent
  OrderService retrieveOrder(Long orderId, Handler<AsyncResult<Order>> resultHandler);
}
