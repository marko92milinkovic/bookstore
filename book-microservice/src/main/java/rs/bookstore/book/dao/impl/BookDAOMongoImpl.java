/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.dao.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import java.util.stream.Collectors;
import rs.bookstore.book.dao.BookDAO;
import rs.bookstore.book.domain.Book;

public class BookDAOMongoImpl extends BookDAO {

    private final MongoClient mongoClient;

    public BookDAOMongoImpl(Vertx vertx, JsonObject config) {
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
    public Future<Book> retrieveOne(Object id) {
        Future<Book> bookFuture = Future.future();

        mongoClient.findOne(COLLECTION,
                new JsonObject().put("_id", id),
                new JsonObject(), ar -> {
                    if (ar.succeeded()) {
                        bookFuture.complete(Book.mapId(ar.result()));
                    } else {
                        bookFuture.fail(ar.cause());
                    }
                });

//        JsonObject command = new JsonObject()
//                .put("aggregate", COLLECTION)
//                .put("project",
////                        {mojID:"$_id", title:1, _id:0}
//                        new JsonObject().put("bookid", id).put("title", 1));
//        mongoClient.runCommand("aggregate", command, ar-> {
//            if(ar.succeeded()) {
//                System.out.println("REZULTAT: "+ar.result());
//            } else {
//                bookFuture.fail(ar.cause());
//            }
//        });
        return bookFuture;
    }

    @Override
    public Future<List<Book>> retrieveAll() {
        Future<List<Book>> future = Future.future();
        mongoClient.find(COLLECTION, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                System.out.println("Books found" + ar.result());
                List<Book> result = ar.result().stream()
                        .map(Book::mapId)
                        .collect(Collectors.toList());
                System.out.println("After converting: " + result);
                future.complete(result);
            } else {
                future.fail(ar.cause());
            }
        });

        return future;
    }
    private static final String COLLECTION = "books";

}
