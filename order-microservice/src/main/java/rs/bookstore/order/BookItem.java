/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.order;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import rs.bookstore.book.domain.Book;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class BookItem {
    
    private long bookId;
    private double price;
    private int amount;
    
    public BookItem(Book book, int amount) {
        this.bookId = book.getBookId();
        this.price = book.getPages();
    }

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

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
    
}
