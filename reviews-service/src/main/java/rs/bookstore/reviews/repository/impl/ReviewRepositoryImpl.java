/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.reviews.repository.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import java.util.List;
import java.util.stream.Collectors;
import rs.bookstore.reviews.repository.ReviewRepository;
import rs.bookstore.reviews.Review;

public class ReviewRepositoryImpl implements ReviewRepository {

    JDBCClient client;

    public ReviewRepositoryImpl(Vertx vertx, JsonObject config) {
        
        this.client = JDBCClient.createNonShared(vertx, config);
    }

    @Override
    public Future<List<Review>> retrieveByBookId(Long bookId) {

        Future<List<Review>> resultFuture = Future.future();

        client.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                connection.queryWithParams(RETRIEVE_BY_BOOKID_STATEMENT, new JsonArray().add(bookId), arRS -> {
                    if (arRS.succeeded()) {
                        ResultSet resultSet = arRS.result();
                        List<Review> collect = resultSet.getRows().stream().map(Review::new).collect(Collectors.toList());
                        resultFuture.complete(collect);
                    } else {
                        resultFuture.fail(arRS.cause());
                    }
                });
            } else {
                resultFuture.fail(ar.cause());
            }

        });

        return resultFuture;

    }

    @Override
    public Future<Void> add(Review entity) {

        JsonArray params = new JsonArray()
                .add(entity.getBookId())
                .add(entity.getUserId())
                .add(entity.getRate())
                .add(entity.getComment());

        Future<Void> resultFuture = Future.future();
        client.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                connection.updateWithParams(SAVE_STATEMENT, params, arUR -> {
                    if (arUR.succeeded()) {
                        resultFuture.complete();
                        connection.close();
                    } else {
                        resultFuture.fail(arUR.cause());
                    }
                });
            } else {
                resultFuture.fail(ar.cause());
            }
        });

        return resultFuture;
    }

    @Override
    public Future<Review> update(Review entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Future<Review> delete(Review entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Future<Review> retrieveOne(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Future<List<Review>> retrieveAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static final String SAVE_STATEMENT = "INSERT INTO `review` (`bookId`, `userid`, `rate`, `comment`) "
            + "VALUES (?, ?, ?, ?)";

    private static final String RETRIEVE_STATEMENT = "SELECT * FROM `review` WHERE id = ?";

    private static final String RETRIEVE_BY_BOOKID_STATEMENT = "SELECT * FROM review \n"
            + "WHERE bookId =? ";
}
