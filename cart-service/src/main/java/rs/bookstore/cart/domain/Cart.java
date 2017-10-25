/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import rs.bookstore.cart.domain.CartEvent.CartEventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class Cart {

    private List<CartItem> cartItems;
    private long customerId;
    private double total;

    public Cart(long customerId) {
        cartItems = new ArrayList<>();
        this.customerId = customerId;
    }

    public Cart(JsonObject json) {
        CartConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        CartConverter.toJson(this, json);
        return json;
    }

 public Cart addEvent(CartEvent cartEvent) {
        if(cartEvent.getCustomerId() != customerId) {
            throw new IllegalArgumentException();
        }
        // The cart event must be a add or remove command event.
        boolean validCartEventType = Stream.of(CartEventType.ADD_ITEM, CartEventType.REMOVE_ITEM)
                .anyMatch(cartEventType
                        -> cartEventType.equals(cartEvent.getCartEventType()));

        if (validCartEventType) {
            Optional <CartItem> optionalItem = cartItems.stream()
                    .filter(cartItem -> cartItem.getBookId() == cartEvent.getBookId())
                    .findFirst();

            if (optionalItem.isPresent()) {
                CartItem cartItem = optionalItem.get();
                int amount = cartItem.getAmount();
                cartItem.setAmount(amount+ (cartEvent.getAmount() * (cartEvent.getCartEventType()
                        .equals(CartEventType.ADD_ITEM) ? 1 : -1)));
            } else {
                cartItems.add(new CartItem(cartEvent.getBookId(), cartEvent.getAmount()));
            }
        }

        return this;
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }


    public List<CartItem> getCartItems() {
        return cartItems;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "cartItems=" + cartItems +
                ", customerId=" + customerId +
                ", total=" + total +
                '}';
    }
}
