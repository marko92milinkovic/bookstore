/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.List;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class Book {

    private long bookId;
    private String title;
    private int year;
    private int pages;
    private double price;
    private String shortDescription;
    private List<String> authors;

    public Book() {
    }

    public Book(JsonObject json) {
        System.out.println("Knjiga: "+json);
        json.put("bookId", json.getLong("_id", json.getLong("bookId", 8888l)));
        BookConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        BookConverter.toJson(this, json);
        return json;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return "Book{" + "bookId=" + bookId + ", title=" + title + ", year=" + year + ", pages=" + pages + ", price=" + price + ", shortDescription=" + shortDescription + ", authors=" + authors + '}';
    }

}
