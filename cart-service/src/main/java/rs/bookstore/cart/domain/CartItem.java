package rs.bookstore.cart.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class CartItem {

    private long bookId;
    private int amount;

    public CartItem(long bookId, int amount) {
        this.bookId = bookId;
        this.amount = amount;
    }

    public  CartItem(JsonObject json) {
        CartItemConverter.fromJson(json, this);
    }

    public JsonObject fromJson () {
        JsonObject json = new JsonObject();
        CartItemConverter.toJson(this, json);
        return json;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }


    @Override
    public String toString() {
        return "CartItem{" + "bookId=" + bookId +", amount=" + amount + '}';
    }

}
