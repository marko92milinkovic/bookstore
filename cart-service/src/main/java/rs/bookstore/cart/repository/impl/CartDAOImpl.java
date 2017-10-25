package rs.bookstore.cart.repository.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import rs.bookstore.cart.domain.Cart;
import rs.bookstore.cart.repository.CartDAO;
import rx.Observable;
import rx.Single;

import java.util.Optional;

public class CartDAOImpl implements CartDAO {


    private final JDBCClient client;

    public CartDAOImpl(Vertx vertx, JsonObject jsonObject) {
        client = JDBCClient.createShared(vertx, jsonObject);
    }
    @Override
    public Single<Optional<Cart>> retrieveOne(Long aLong) {
        return null;
    }

    @Override
    public Single <Cart> updateOne(Long aLong, Cart entity) {
        return null;
    }

    @Override
    public Single <Void> addOne(Cart entity) {

        JsonArray params = new JsonArray()
                .add(entity.getCustomerId())
                .add(entity.getTotal());
        return client.rxGetConnection()
                .flatMap(conn -> conn.rxQueryWithParams(SAVE_STATEMENT, params)
                        .map(r -> (Void) null)
                        .doAfterTerminate(conn::close)
                );
    //to do:: save items

    }

    @Override
    public Observable<Cart> retrieveAll() {
        return null;
    }

    @Override
    public Single <Cart> deleteOne(Long aLong) {
        return null;
    }

    private static final String SAVE_STATEMENT = "INSERT INTO `cart` "
            + "(`customerId`, `total`)"
            + "VALUES (?, ?)";
    private static final String SAVE_ITEM_STATEMENT = "INSERT INTO `cart_item` "
            + "(`book_id`, `amount`)"
            + "VALUES (?, ?)";
}
