/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart.repository;

import rs.bookstore.cart.domain.CartEvent;
import rs.bookstore.lib.repository.RxRepository;
import rx.Observable;

/**
 *
 * @author marko
 */
public interface CartEventDAO extends RxRepository<CartEvent, Long> {

    Observable<CartEvent> streamByCustomerId(Long customerId);
}
