/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.reviews;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import rs.bookstore.constants.PortsConstants;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author markom
 */
public class ReviewsApiVerticle extends MicroServiceVerticle {

    ReviewService service;

    @Override
    public void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.

        service = new ReviewServiceImpl(vertx, config());

        Vertx rxVertx = Vertx.newInstance(vertx);

        Router router = Router.router(rxVertx);

        router.route().handler(BodyHandler.create());
        router.post(ADD).handler(this::addReview);
        router.get(GET_ALL_BY_BOOKID).handler(this::retrieveAllByBookId);

        int httpPort = config().getInteger("http.port", PortsConstants.REVIEW_SERVICE_PORT);
        String host = config().getString("host", "localhost");
        rxVertx.createHttpServer().requestHandler(router::accept).listen(httpPort, host);

    }

    private void addReview(RoutingContext rc) {
        JsonObject reviewAsJson = rc.getBodyAsJson();
        System.out.println("Review as JSON: " + reviewAsJson);
        Review review = new Review(reviewAsJson);
        service.addReview(review, ar -> {
            if (ar.succeeded()) {
                rc.response().end("added");
            } else {
                rc.response().end(ar.cause().getMessage());
            }
        });
    }

    private void retrieveAllByBookId(RoutingContext rc) {
        Long bookId;
        HttpServerResponse response = rc.response();

        try {
            bookId = Long.parseLong(rc.pathParam("bookId"));
        } catch (NumberFormatException e) {
            response.setStatusCode(400).end("book id must be long");
            return;
        }
        
        service.getReviewsByBookId(bookId, ar-> {
            if(ar.succeeded()) {
                response.end(Json.encodePrettily(ar.result()));
            } else {
                response.setStatusCode(500).end(ar.cause().getMessage());
            }
        });
    }

    private final static String ADD = "/reviews";
    private final static String GET_ALL_BY_BOOKID = "/reviews/bookId/:bookId";

}
