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
    private String title;
    private double price;
    private int amount;
    
    public BookItem(Book book, int amount) {
        this.bookId = book.getBookId();
        this.price = book.getPrice();
        this.title = book.getTitle();
        this.amount = amount;
    }

    public BookItem(long bookId, double price, int amount, String title) {
        this.bookId = bookId;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "BookItem{" + "bookId=" + bookId + ", title=" + title + ", price=" + price + ", amount=" + amount + '}';
    }

}
