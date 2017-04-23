/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.reviews.repository.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.core.Vertx;
import java.util.Optional;
import rs.bookstore.reviews.Review;
import rx.Observable;
import rx.Single;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import rs.bookstore.reviews.repository.ReviewRxRepository;

public class ReviewRxRepositoryImpl implements ReviewRxRepository {

    JDBCClient client;

    public ReviewRxRepositoryImpl(io.vertx.core.Vertx vertx, JsonObject config) {
        this.client = JDBCClient.createNonShared(Vertx.newInstance(vertx), config);
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
        JsonArray params = new JsonArray()
                .add(entity.getBookId())
                .add(entity.getUserId())
                .add(entity.getRate())
                .add(entity.getComment());

        return client.rxGetConnection()
                .flatMap(conn -> conn.rxUpdateWithParams(SAVE_STATEMENT, params)
                        .map(r -> (Void) null)
                        .doAfterTerminate(conn::close));

    }

    @Override
    public Observable<Review> retrieveByBookId(Long bookId) {
        return client.rxGetConnection()
                .flatMapObservable(conn
                        -> conn.rxQueryWithParams(RETRIEVE_BY_BOOKID_STATEMENT, new JsonArray().add(bookId))
                        .map(ResultSet::getRows)
                        .flatMapObservable(Observable::from)
                        .map(Review::new)
                        .doOnTerminate(conn::close));
    }

    @Override
    public Observable<Review> retrieveAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<Review> deleteOne(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static final String SAVE_STATEMENT = "INSERT INTO `review` (`bookId`, `userid`, `rate`, `comment`) "
            + "VALUES (?, ?, ?, ?)";

    private static final String RETRIEVE_STATEMENT = "SELECT * FROM `review` WHERE id = ?";

    private static final String RETRIEVE_BY_BOOKID_STATEMENT = "SELECT * FROM review \n"
            + "WHERE bookId =? "
            + "ORDER BY reviewId DESC";

}
