/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.book.api;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import rs.bookstore.book.domain.Book;
import rs.bookstore.book.service.BookService;
import rs.bookstore.book.service.impl.BookServiceImpl;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author marko
 */
public class BookAPIVerticle extends MicroServiceVerticle {

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

        //API gateway can ensure if the endpoint is active
//        enableHeartbeatCheck(router, config());
        //Retrieve port,host and START SERVER
        String host = config().getString("service.book.host", "localhost");
        int port = config().getInteger("service.book.port", 9002);

        Future<Void> future = Future.future();

        createHttpServer(router, host, port)
                .compose(server -> {
                    publishHttpEndpoint(host, host, port, future);
                }, future)
                .setHandler(startFuture.completer());

    }

    private void addBook(RoutingContext routingContext) {
        JsonObject bookAsJson = routingContext.getBodyAsJson();
        Book book = new Book(bookAsJson);

        bookService.addBook(book, ar -> {
            if (ar.failed()) {
                routingContext.fail(ar.cause());
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
                routingContext.fail(ar.cause());
                routingContext.response()
                        .setStatusCode(404)
                        .end("Book not found");
            } else {
                response.end(ar.result().toJson().encodePrettily());
            }
        });
    }

    private void retrieveAll(RoutingContext rc) {
        bookService.getAllBooks(ar -> {
            if (ar.failed()) {
                System.out.println("Failed: " + ar.cause());
                rc.response().setStatusCode(500).end("Server error");
            } else {
                System.out.println("RESULT: "+ar.result());
                rc.response().end(new JsonArray(ar.result()).encodePrettily());
            }
        });
    }

    private Future<HttpServer> createHttpServer(Router router, String host, int port) {
        Future<HttpServer> future = Future.future();
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(port, host, future.completer());
        return future;
    }

    private static final String API_ADD = "/add";
    private static final String API_GET_ONE = "/books/:bookId";
    private static final String API_GET_ALL = "/books";
    private static final String API_DELETE = "/:bookId";
    private static final String API_UPDATE = "/:bookId";

}
