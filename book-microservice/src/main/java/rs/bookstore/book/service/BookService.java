/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import rs.bookstore.book.domain.Book;

/**
 *
 * This is an event bus service so it is annotated with @ProxyGen
 *
 * @author marko
 */
//Polyglot support
@VertxGen
//
@ProxyGen
public interface BookService {

    /**
     * The address on which the service is published
     */
    String SERVICE_ADDRESS = "service.book";

    //These methods are asynchronous so they need to accept a Handler<AsyncResult<T>>
    //When the invocation is ready, the handler will be called
    @Fluent
    BookService addBook(Book book, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    BookService getBook(long bookID, Handler<AsyncResult<Book>> resultHandler);

    @Fluent
    BookService getAllBooks(Handler<AsyncResult<List<Book>>> resultHandler);

    @Fluent
    BookService updateBook(long bookID, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    BookService deleteBook(long bookID, Handler<AsyncResult<Book>> resultHandler);

}
