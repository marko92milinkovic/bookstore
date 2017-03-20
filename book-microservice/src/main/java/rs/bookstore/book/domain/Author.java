/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.List;

/**
 *
 * @author marko
 */
@DataObject(generateConverter = true)
public class Author {

    private String name;
    private List<Book> books;

    public Author() {
    }

    public Author(JsonObject json) {
        AuthorConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        AuthorConverter.toJson(this, json);
        return json;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonBackReference
    public List<Book> getBooks() {
        return books;
    }

    @JsonBackReference
    public void setBooks(List<Book> books) {
        this.books = books;
    }

}
