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
public class BookItem {
    
    long bookId;
    double price;
    int amount;

    public BookItem(long bookId, double price, int amount) {
        this.bookId = bookId;
        this.price = price;
        this.amount = amount;
    }
    
    public BookItem (JsonObject json) {
        BookItemConverter.fromJson(json, this);
    }
    
    public JsonObject fromJson () {
        JsonObject json = new JsonObject();
        BookItemConverter.toJson(this, json);
        return json;
    }
    
}
