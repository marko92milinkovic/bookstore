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

    public Order(long orderId, long customerId, long paymentId, List<BookItem> bookItems, double totalPrice) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentId = paymentId;
        this.bookItems = bookItems;
        this.totalPrice = totalPrice;
    }

    public Order(JsonObject json) {
        OrderConverter.fromJson(json, this);
    }
    
    public JsonObject toJson () {
        JsonObject json = new JsonObject();
        OrderConverter.fromJson(json, this);
        return json;
    }
}
