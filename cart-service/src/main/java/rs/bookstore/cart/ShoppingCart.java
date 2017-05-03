/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart;

import java.util.List;
import java.util.stream.Stream;
import rs.bookstore.cart.event.CartEvent;
import rs.bookstore.cart.event.CartEvent.CartEventType;

/**
 *
 * @author marko
 */
public class ShoppingCart {

    List<BookItem> bookItems;

    
    
    public ShoppingCart incorporate(CartEvent cartEvent) {
        // The cart event must be a add or remove command event.
        boolean ifValid = Stream.of(CartEventType.ADD_ITEM, CartEventType.REMOVE_ITEM)
                .anyMatch(cartEventType
                        -> cartEvent.getCartEventType().equals(cartEventType));

//        if (ifValid) {
//            amountMap.put(cartEvent.getBookId(),
//                    amountMap.getOrDefault(cartEvent.getProductId(), 0)
//                    + (cartEvent.getAmount() * (cartEvent.getCartEventType()
//                    .equals(CartEventType.ADD_ITEM) ? 1 : -1)));
//        }

        return this;
    }

}
