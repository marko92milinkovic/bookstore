/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart.repository.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import java.util.Optional;
import rs.bookstore.cart.domain.CartEvent;
import rx.Observable;
import rx.Single;
import rs.bookstore.cart.repository.CartEventDAO;

public class CartEventDAOImpl implements CartEventDAO {

    private final JDBCClient client;

    public CartEventDAOImpl(Vertx vertx, JsonObject jsonObject) {
        client = JDBCClient.createNonShared(vertx, jsonObject);
    }

    @Override
    public Observable<CartEvent> streamByCustomerId(Long customerId) {
        return client.rxGetConnection()
                .flatMapObservable(conn
                        -> conn.rxQueryWithParams(STREAM_STATEMENT, new JsonArray().add(customerId))
                        .map(ResultSet::getRows)
                        .flatMapObservable(Observable::from)
                        .map(CartEvent::new)
                        .doOnTerminate(conn::close)
                );
    }

    @Override
    public Single<Optional<CartEvent>> retrieveOne(Long id) {
        return client.rxGetConnection()
                .flatMap(conn
                        -> conn.rxQueryWithParams(RETRIEVE_STATEMENT, new JsonArray().add(id))
                        .map(ResultSet::getRows)
                        .map(list -> {
                            if (list.isEmpty()) {
                                return Optional.<CartEvent>empty();
                            } else {
                                return Optional.of(list.get(0))
                                        .map(CartEvent::new);
                            }
                        })
                        .doAfterTerminate(conn::close)
                );
    }

    @Override
    public Single<CartEvent> updateOne(Long id, CartEvent entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<Void> addOne(CartEvent entity) {
        JsonArray params = new JsonArray().add(entity.getCartEventType().name())
                .add(entity.getCustomerId())
                .add(entity.getBookId())
                .add(entity.getAmount())
                .add(entity.getTime() > 0 ? entity.getTime() : System.currentTimeMillis());
        return client.rxGetConnection()
                .flatMap(conn -> conn.rxUpdateWithParams(SAVE_STATEMENT, params)
                .map(r -> (Void) null)
                        .doAfterTerminate(conn::close)
                );
    }

    @Override
    public Observable<CartEvent> retrieveAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Single<CartEvent> deleteOne(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static final String SAVE_STATEMENT = "INSERT INTO `cart_event` "
            + "(`type`, `customerId`, `bookId`, `amount`, `make_time`) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String RETRIEVE_STATEMENT = "SELECT * FROM `cart_event` WHERE id = ?";

    private static final String STREAM_STATEMENT = "SELECT * FROM cart_event c\n"
            + "WHERE c.customerId = ? AND c.make_time > coalesce(\n"
            + "    (SELECT make_time FROM cart_event\n"
            + "\t WHERE customerId = c.customerId AND (`type` = \"CHECKOUT\" OR `type` = \"CLEAR_CART\")\n"
            + "     ORDER BY cart_event.make_time DESC\n"
            + "     LIMIT 1\n"
            + "     ), 0)\n"
            + "ORDER BY c.make_time ASC;";

}
