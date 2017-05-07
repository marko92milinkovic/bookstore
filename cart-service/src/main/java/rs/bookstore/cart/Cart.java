/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import rs.bookstore.cart.event.CartEvent;
import rs.bookstore.cart.event.CartEvent.CartEventType;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class Cart {

    List<BookItem> bookItems;

    public Cart() {
        bookItems = new ArrayList<>();
    }
    
    

    public Cart(JsonObject json) {
        CartConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        CartConverter.toJson(this, json);
        return json;
    }

    @Deprecated
    public Cart incorporate(CartEvent cartEvent) {
        // The cart event must be a add or remove command event.
       Stream.of(CartEventType.ADD_ITEM, CartEventType.REMOVE_ITEM)
                .anyMatch(cartEventType
                        -> cartEvent.getCartEventType().equals(cartEventType));
        return this;
    }

    @Override
    public String toString() {
        return toJson().encode();
    }    
}
