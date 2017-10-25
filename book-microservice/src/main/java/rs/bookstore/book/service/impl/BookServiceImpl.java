/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.bookstore.book.dao.BookDAO;
import rs.bookstore.book.domain.Book;
import rs.bookstore.book.service.BookService;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private static final String COLLECTION = "books";
    @Autowired
    private BookDAO bookDAO;

    public BookServiceImpl() {

    }

    @Override
    public BookService addBook(Book book, Handler<AsyncResult<Void>> resultHandler) {
        bookDAO.add(book).setHandler(resultHandler);
        return this;
    }

    @Override
    public BookService getBook(long bookID, Handler<AsyncResult<Book>> resultHandler) {
        bookDAO.retrieveOne(bookID).setHandler(resultHandler);
        return this;
    }

    @Override
    public BookService getAllBooks(Handler<AsyncResult<List<Book>>> resultHandler) {
        System.out.println("Daj bre knjige");
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

}
