/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.reviews;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.List;
import rs.bookstore.reviews.repository.ReviewRxRepository;
import rs.bookstore.reviews.repository.impl.ReviewRxRepositoryImpl;

public class ReviewServiceImpl implements ReviewService {

    ReviewRxRepository repository;

    public ReviewServiceImpl(Vertx vertx, JsonObject config) {
        repository = new ReviewRxRepositoryImpl(vertx, config);
    }

    @Override
    public void addReview(Review review, Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> resultFuture = Future.future();
        repository.addOne(review).subscribe(resultFuture::complete, resultFuture::fail);
        resultFuture.setHandler(resultHandler);
    }

    @Override
    public void getReviewsByBookId(Long bookId, Handler<AsyncResult<List<Review>>> resultHandler) {
        Future<List<Review>> resultFuture = Future.future();
        repository.retrieveByBookId(bookId).toList().subscribe(resultFuture::complete, resultFuture::fail);
        resultFuture.setHandler(resultHandler);
    }

    @Override
    public void getAllReviews(Handler<AsyncResult<List<Review>>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}