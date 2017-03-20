/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import java.util.stream.Collectors;
import rs.bookstore.book.dao.BookDAO;
import rs.bookstore.book.dao.BookDAOMongoImpl;
import rs.bookstore.book.domain.Book;
import rs.bookstore.book.service.BookService;

public class BookServiceImpl implements BookService {

//    private final MongoClient mongoClient;
    private final BookDAO bookDAO;

    public BookServiceImpl(Vertx vertx, JsonObject config) {
//        mongoClient = MongoClient.createShared(vertx, config);
        bookDAO = new BookDAOMongoImpl(vertx, config);
    }

    @Override
    public BookService addBook(Book book, Handler<AsyncResult<Void>> resultHandler) {
        bookDAO.add(book).setHandler(resultHandler);
        return this;
    }

    @Override
    public BookService getBook(long bookID, Handler<AsyncResult<Book>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BookService getAllBooks(Handler<AsyncResult<List<Book>>> resultHandler) {

        Future<List<Book>> retrieveAll = bookDAO.retrieveAll();
        retrieveAll.setHandler(resultHandler);
        return this;
    }

    @Override
    public BookService updateBook(long bookID, Handler<AsyncResult<Void>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BookService deleteBook(long bookID, Handler<AsyncResult<Book>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private static final String COLLECTION = "books";

}
