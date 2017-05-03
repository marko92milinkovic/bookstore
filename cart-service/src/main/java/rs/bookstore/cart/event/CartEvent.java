/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart.event;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class CartEvent {

    private long cartEventId;
    private CartEventType cartEventType;
    private long customerId;
    private long bookId;
    private int amount;
    private long time = System.currentTimeMillis();

    public CartEvent() {
    }

    public CartEvent(JsonObject json) {
        CartEventConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        CartEventConverter.fromJson(json, this);
        return json;
    }

    public CartEvent(CartEventType cartEventType, long customerId, long bookId, int amount) {
        this.cartEventType = cartEventType;
        this.customerId = customerId;
        this.bookId = bookId;
        this.amount = amount;
    }

    public static CartEvent createCheckoutEvent(long customerId) {
        return new CartEvent(CartEventType.CHECKOUT, customerId, 0, 0);
    }

    public static CartEvent createClearEvent(long customerId) {
        return new CartEvent(CartEventType.CLEAR_CART, customerId, 0, 0);
    }

    public static boolean isTerminal(CartEventType eventType) {
        return eventType == CartEventType.CLEAR_CART || eventType == CartEventType.CHECKOUT;
    }

    public long getCartEventId() {
        return cartEventId;
    }

    public void setCartEventId(long cartEventId) {
        this.cartEventId = cartEventId;
    }

    public CartEventType getCartEventType() {
        return cartEventType;
    }

    public void setCartEventType(CartEventType cartEventType) {
        this.cartEventType = cartEventType;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public enum CartEventType {
        ADD_ITEM, // add an item to the cart
        REMOVE_ITEM, // remove an item from the cart
        CHECKOUT, // shopping cart checkout
        CLEAR_CART // clear the cart
    }
}
