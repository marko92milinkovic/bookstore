/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.reviews;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.List;
import java.util.Optional;
import rs.bookstore.lib.repository.RxRepository;
import rx.Observable;
import rx.Single;


public class ReviewServiceImpl implements ReviewService, RxRepository<Review, Long> {

    @Override
    public void addReview(Review review, Handler<AsyncResult<Void>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getAllReviews(Handler<AsyncResult<List<Review>>> resultHandler) {
        Observable<Review> reviews = retrieveAll();
    }

    @Override
    public Single<Optional<Review>> retrieveOne(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<Review> updateOne(Long id, Review entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<Void> addOne(Review entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Observable<Review> retrieveAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<Review> deleteOne(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
