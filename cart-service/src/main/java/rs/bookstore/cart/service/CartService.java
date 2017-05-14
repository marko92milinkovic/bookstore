/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import rs.bookstore.cart.Cart;
import rs.bookstore.cart.CheckoutResult;
import rs.bookstore.cart.event.CartEvent;

/**
 *
 * @author marko
 */
@VertxGen(concrete = false)
@ProxyGen
public interface CartService {

    void addCartEvent(CartEvent event, Handler<AsyncResult<Void>> resultHandler);
    void getCart(Long customerId, Handler<AsyncResult<Cart>> resultHandler);
    void checkout(Long customerId, Handler<AsyncResult<CheckoutResult>> resultHandler);

}
