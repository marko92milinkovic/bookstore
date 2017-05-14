/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import rs.bookstore.cart.event.CartEvent;
import rs.bookstore.cart.event.CartEvent.CartEventType;
import rs.bookstore.order.BookItem;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class Cart {

    private List<BookItem> bookItems;
    @GenIgnore
    private Map<Long, Integer> amountMap;

    public Cart() {
        this.amountMap = new HashMap<>();
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

    /**
     * Incorporates a new {@link CartEvent} and updated the shopping cart
     *
     * @param cartEvent is the {@link CartEvent} that will alter the state of
     * the cart
     * @return the state of the {@link ShoppingCart} after applying the new
     * {@link CartEvent}
     */
    public Cart incorporate(CartEvent cartEvent) {
        // The cart event must be a add or remove command event.
        boolean validCartEventType = Stream.of(CartEventType.ADD_ITEM, CartEventType.REMOVE_ITEM)
                .anyMatch(cartEventType
                        -> cartEventType.equals(cartEvent.getCartEventType()));

        if (validCartEventType) {
            // Update the aggregate view of each line item's quantity from the event type
            amountMap.put(cartEvent.getBookId(),
                    amountMap.getOrDefault(cartEvent.getBookId(), 0)
                    + (cartEvent.getAmount() * (cartEvent.getCartEventType()
                    .equals(CartEventType.ADD_ITEM) ? 1 : -1)));
        }

        return this;
    }

    @GenIgnore
    public Map<Long, Integer> getAmountMap() {
        return amountMap;
    }

    public void setBookItems(List<BookItem> items) {
        this.bookItems = items;
    }

    public List<BookItem> getBookItems() {
        return bookItems;
    }

    @Override
    public String toString() {
        return "Cart{" + "bookItems=" + bookItems + ", amountMap=" + amountMap + '}';
    }

}
