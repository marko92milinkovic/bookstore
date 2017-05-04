/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.repository.impl;

import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.Optional;
import rs.bookstore.order.Order;
import rs.bookstore.order.repository.OrderRxRepository;
import rx.Observable;
import rx.Single;


public class OrderRxRepositoryImpl implements OrderRxRepository {

    @Override
    public Observable<Order> retrieveOrdersByCustomer(Long customerId) {
        return Observable.from(Arrays.asList(new Order(new JsonObject()), new Order(new JsonObject())));
    }

    @Override
    public Single<Optional<Order>> retrieveOne(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<Order> updateOne(Long id, Order entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<Void> addOne(Order entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Observable<Order> retrieveAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<Order> deleteOne(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
