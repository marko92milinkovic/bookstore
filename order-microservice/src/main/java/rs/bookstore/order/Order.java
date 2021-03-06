/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.List;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class Order {

    private long orderId;
    private long customerId;
    private long paymentId;
    private List<BookItem> bookItems;
    private double totalPrice;
    private long createTime;

    public Order(long orderId, long customerId, long paymentId, List<BookItem> bookItems, double totalPrice, long createTime) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentId = paymentId;
        this.bookItems = bookItems;
        this.totalPrice = totalPrice;
        this.createTime = createTime;
    }

    public Order() {
    }

    public Order(JsonObject json) {
        OrderConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        OrderConverter.fromJson(json, this);
        return json;
    }

    public long getOrderId() {
        return orderId;
    }

    public Order setOrderId(long orderId) {
        this.orderId = orderId;
        return this;
    }

    public long getCustomerId() {
        return customerId;
    }

    public Order setCustomerId(long customerId) {
        this.customerId = customerId;
        return this;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public Order setPaymentId(long paymentId) {
        this.paymentId = paymentId;
        return this;

    }

    public List<BookItem> getBookItems() {
        return bookItems;
    }

    public Order setBookItems(List<BookItem> bookItems) {
        this.bookItems = bookItems;
        return this;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Order setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
