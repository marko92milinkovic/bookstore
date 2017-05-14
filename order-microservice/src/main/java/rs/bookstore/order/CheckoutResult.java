/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class CheckoutResult {
    
    private String resultMessage;
    private Order order;
    
    public CheckoutResult(JsonObject json) {
        CheckoutResultConverter.fromJson(json, this);
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        CheckoutResultConverter.toJson(this, json);
        return json;
    }

    public CheckoutResult() {
    }
    

    public String getResultMessage() {
        return resultMessage;
    }

    public CheckoutResult setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
        return this;
    }

    public Order getOrder() {
        return order;
    }

    public CheckoutResult setOrder(Order order) {
        this.order = order;
        return this;
    }
    
}
