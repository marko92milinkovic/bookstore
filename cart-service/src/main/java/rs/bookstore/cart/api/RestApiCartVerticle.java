/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart.api;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.rx.java.RxHelper;
import java.util.Optional;
import java.util.function.BiConsumer;
import rs.bookstore.cart.Cart;
import rs.bookstore.cart.Checkout;
import rs.bookstore.cart.event.CartEvent;
import rs.bookstore.cart.service.CartService;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author marko
 */
public class RestApiCartVerticle extends MicroServiceVerticle {
    
    private final CartService cartService;
    
    public RestApiCartVerticle(CartService cartService) {
        this.cartService = cartService;
    }
    
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        
        router.post(API_ADD_CART_EVENT).handler(rc -> requireLogin(rc, this::addCartEvent));
        router.get(API_GET_CART).handler(rc -> requireLogin(rc, this::getCart));
        router.post(API_CHECKOUT).handler(rc -> requireLogin(rc, this::checkout));

        //enable local session
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx, "shopping.session")));
        
    }
    
    private void addCartEvent(RoutingContext rc, Long customerId) {
        CartEvent cartEvent = new CartEvent(rc.getBodyAsJson());
        if (cartEvent.getAmount() > 0 && cartEvent.getCustomerId() == customerId) {
            cartService.addCartEvent(cartEvent, ar -> {
                if (ar.succeeded()) {
                    rc.response().setStatusCode(201).end("Event added");
                } else {
                    rc.response().end(ar.cause().toString());
                }
            });
        } else {
            rc.fail(400);
        }
    }
    
    private void getCart(RoutingContext rc, Long customerId) {
        cartService.getCart(customerId, RxHelper.<Cart>toFuture(
                cart -> rc.response().end(Json.encode(cart)),
                cause -> rc.response().setStatusCode(400).end(cause.getMessage())));
    }
    
    private void checkout(RoutingContext rc, Long customerId) {
        cartService.checkout(customerId, RxHelper.<Checkout>toFuture(
                checkout -> rc.response().end(checkout.toJson().encode()),
                cause -> rc.response().setStatusCode(400).end(cause.getMessage())));
    }
    
    protected void requireLogin(RoutingContext context, BiConsumer<RoutingContext, Long> biHandler) {
        Optional<JsonObject> principal = Optional.ofNullable(context.request().getHeader("user-principal"))
                .map(JsonObject::new);
        if (principal.isPresent()) {
            biHandler.accept(context, principal.get().getLong("customer_id"));
        } else {
            context.response()
                    .setStatusCode(401)
                    .end(new JsonObject().put("message", "need_auth").encode());
        }
    }
    
    private static final String API_CHECKOUT = "/checkout";
    private static final String API_ADD_CART_EVENT = "/events/add";
    private static final String API_GET_CART = "/cart";
}
