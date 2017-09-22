/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.impl.MessageImpl;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
import io.vertx.serviceproxy.ProxyHelper;
import rs.bookstore.book.domain.Book;
import rs.bookstore.book.service.BookService;

import static rs.bookstore.book.service.BookService.SERVICE_ADDRESS;
import rs.bookstore.book.service.impl.BookServiceImpl;
import rs.bookstore.constants.MicroServiceNamesConstants;
import rs.bookstore.lib.MicroServiceVerticle;
import rx.Observer;
import rx.Single;

import java.util.List;


/**
 *
 * @author marko
 */
public class BookRestAPIVerticle extends MicroServiceVerticle {

    private BookService bookService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        bookService = new BookServiceImpl(vertx, config());
        final Router router = Router.router(vertx);
        //body handler
        router.route().handler(BodyHandler.create());
        //API route handler
        router.post(API_ADD).handler(this::addBook);
        router.get(API_GET_ONE).handler(this::getOne);
        router.get(API_GET_ALL).handler(this::retrieveAll);
        ProxyHelper.registerService(BookService.class, vertx, bookService, SERVICE_ADDRESS);

        //API gateway can ensure if the endpoint is active
//        enableHeartbeatCheck(router, config());
        //Retrieve port,host and START SERVER
        String host = config().getString("service.book.host", "localhost");
        int port = config().getInteger("service.book.port", 9002);

        createHttpServer(router, host, port)
                .compose(server -> {
                    Future<Void> httpFuture = Future.future();
                    publishHttpEndpoint(MicroServiceNamesConstants.BOOK_SERVICE_HTTP, host, port, httpFuture);
                    return httpFuture;
                })
                .compose(httpF -> {
                    Future<Void> rpcFuture = Future.future();
                    publishEventBusService(MicroServiceNamesConstants.BOOK_SERVICE_RPC,
                            BookService.SERVICE_ADDRESS, BookService.class, rpcFuture);
                    return rpcFuture;
                }).setHandler(startFuture.completer());

    }

    private void addBook(RoutingContext routingContext) {
        JsonObject bookAsJson = routingContext.getBodyAsJson();
        Book book = new Book(bookAsJson);

        bookService.addBook(book, ar -> {
            if (ar.failed()) {
                routingContext.response()
                        .setStatusCode(400)
                        .end("Book not added");
            } else {
                routingContext.response()
                        .setStatusCode(201)
                        .end(bookAsJson.encodePrettily());
            }
        });
    }

    private void getOne(RoutingContext routingContext) {
        String bookIdAsString = routingContext.pathParam("bookId").trim();
        HttpServerResponse response = routingContext.response();
        if (bookIdAsString == null || "".equals(bookIdAsString)) {
            response.setStatusCode(400).end("Invalid bookId");
            return;
        }

        long bookId;
        try {
            bookId = Long.parseLong(bookIdAsString);
        } catch (Exception ex) {
            response.setStatusCode(400).end("Invalid bookId");
            return;
        }

        bookService.getBook(bookId, ar -> {
            if (ar.failed()) {
                routingContext.response()
                        .setStatusCode(404)
                        .end("Book not found");
            } else {
                response.end(ar.result().toJson().encodePrettily());
            }
        });
    }

    private void retrieveAll(RoutingContext rc) {
//        bookService.getAllBooks(ar -> {
//            if (ar.failed()) {
//                rc.response().setStatusCode(500).end(ar.cause().getMessage());
//            } else {
//                rc.response().end(new JsonArray(ar.result()).encodePrettily());
//            }
//        });


        Observer<List<Book>> observer = new Observer<List<Book>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                rc.response().setStatusCode(500).end(e.getMessage());
            }

            @Override
            public void onNext(List<Book> bookList) {
                rc.response().end(new JsonArray(bookList).encodePrettily());
            }
        };
//        Handler<AsyncResult<List<Book>>> asyncResultHandler = RxHelper.toFuture(observer);
//
//        Handler<AsyncResult<List<Book>>> asyncResultHandler = RxHelper.toFuture( books -> rc.response().end(new JsonArray(books).encodePrettily()),
//                error-> rc.response().setStatusCode(500).end(error.getMessage()));
//
//        bookService.getAllBooks(asyncResultHandler);


        bookService.getAllBooks(RxHelper.toFuture( books -> rc.response().end(new JsonArray(books).encodePrettily()),
                                                    error-> rc.response().setStatusCode(500).end(error.getMessage())));

//        ObservableFuture<List<Book>> booksObservableFuture = RxHelper.observableFuture();
//
//        booksObservableFuture.subscribe(
//                books -> rc.response().end(new JsonArray(books).encodePrettily()),
//                error -> rc.response().setStatusCode(500).end(error.getMessage())
//        );
//
//        Handler<AsyncResult<List<Book>>> asyncResultHandler = booksObservableFuture.toHandler();
//        bookService.getAllBooks(asyncResultHandler);

    }


    private static final String API_ADD = "/add";
    private static final String API_GET_ONE = "/books/:bookId";
    private static final String API_GET_ALL = "/books";
    private static final String API_DELETE = "/:bookId";
    private static final String API_UPDATE = "/:bookId";

}
