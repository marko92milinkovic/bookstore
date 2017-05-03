/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.List;
import rs.bookstore.order.Order;
import rs.bookstore.order.service.OrderService;


public class OrderServiceImpl implements OrderService {

    public OrderServiceImpl(Vertx vertx, JsonObject config) {
    }
    
    

    @Override
    public OrderService retrieveOrdersForCustomer(Long customerId, Handler<AsyncResult<List<Order>>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OrderService createOrder(Order order, Handler<AsyncResult<Void>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OrderService retrieveOrder(Long orderId, Handler<AsyncResult<Order>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
