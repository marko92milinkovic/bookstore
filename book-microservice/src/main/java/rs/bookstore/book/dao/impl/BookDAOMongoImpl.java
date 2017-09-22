/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.dao.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import rs.bookstore.book.dao.BookDAO;
import rs.bookstore.book.domain.Book;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BookDAOMongoImpl extends BookDAO {

    private static final String COLLECTION = "books";
    @Autowired
    Vertx vertx;
    @Inject
    JsonObject config;
    private MongoClient mongoClient;

    public BookDAOMongoImpl() {
    }

    @PostConstruct
    private void init() {
        System.out.println("Book DAO init");
        mongoClient = MongoClient.createShared(vertx, config);
    }

    @Override
    public Future<Void> add(Book entity) {
        Future<Void> future = Future.future();
        System.out.println("saving book: " + entity.toJson());
        mongoClient.save(COLLECTION, entity.toJson(), ar -> {
            if (ar.succeeded()) {
                future.complete();
            } else {
                future.fail(ar.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Book> update(Book entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Future<Book> delete(Book entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Future<Book> retrieveOne(Long id) {
        Future<JsonObject> jsonFuture = Future.future();

        mongoClient.findOne(COLLECTION,
                new JsonObject().put("_id", id),
                new JsonObject(), jsonFuture.completer());

        Future<Book> bookFuture = jsonFuture.compose(json -> Future.<Book>succeededFuture(new Book(json)));

        return bookFuture;
    }

    @Override
    public Future<List<Book>> retrieveAll() {
        Future<List<Book>> future = Future.future();
        mongoClient.find(COLLECTION, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                List<Book> result = ar.result().stream()
                        .map(Book::new)
                        .collect(Collectors.toList());
                future.complete(result);
            } else {
                future.fail(ar.cause());
            }
        });

        return future;
    }

}
