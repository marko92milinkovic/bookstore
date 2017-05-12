/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import rs.bookstore.order.Order;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class Checkout {
    
    private String resultMessage;
    private Order order;
    
    public Checkout(JsonObject json) {
        CheckoutConverter.fromJson(json, this);
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        CheckoutConverter.toJson(this, json);
        return json;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
    
}
