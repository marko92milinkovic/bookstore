/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.reviews;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.List;

/**
 *
 * @author markom
 */
@VertxGen
@ProxyGen
public interface ReviewService {

    void addReview(Review review, Handler<AsyncResult<Void>> resultHandler);
    void getAllReviews(Handler<AsyncResult<List<Review>>> resultHandler);
    
}
