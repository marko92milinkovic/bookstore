/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order.repository;

import rs.bookstore.lib.repository.RxRepository;
import rs.bookstore.order.Order;
import rx.Observable;

/**
 *
 * @author markom
 */
public interface OrderRxRepository extends RxRepository<Order, Long> {
    Observable<Order> retrieveOrdersByCustomer (Long customerId); 
}
