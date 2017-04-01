/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private List<Author> authors;

    public Book() {
    }

    public Book(JsonObject json) {
        BookConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        BookConverter.toJson(this, json);
        return json;
    }

    //fix this
    public static Book mapId(JsonObject json) {
        JsonObject mapper = json.copy().put("bookId", json.getLong("_id"));
        return new Book(mapper);
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
    
    @JsonManagedReference
    public List<Author> getAuthors() {
        return authors;
    }

    @JsonManagedReference
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
