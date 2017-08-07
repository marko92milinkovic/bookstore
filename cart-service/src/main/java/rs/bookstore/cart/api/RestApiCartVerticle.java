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
import io.vertx.rxjava.core.Vertx;
import java.util.Optional;
import java.util.function.BiConsumer;
import rs.bookstore.cart.Cart;
import rs.bookstore.order.CheckoutResult;
import rs.bookstore.cart.event.CartEvent;
import rs.bookstore.cart.service.CartService;
import rs.bookstore.cart.service.impl.CartServiceImpl;
import rs.bookstore.constants.MicroServiceNamesConstants;
import rs.bookstore.constants.PortsConstants;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author marko
 */
public class RestApiCartVerticle extends MicroServiceVerticle {
    
    private CartService cartService;
    
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        
        router.post(API_ADD_CART_EVENT).handler(rc -> requireLogin(rc, this::addCartEvent));
        router.get(API_GET_CART).handler(rc -> requireLogin(rc, this::getCart));
        router.get(API_CHECKOUT).handler(rc -> requireLogin(rc, this::checkout));
        router.get(API_GET_CART_TEST).handler(rc -> getCart(rc, 1l));

        //enable local session
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx, "shopping.session")));
        System.out.println("\n\n\n\n\n");
        System.out.println(config().encodePrettily());
        cartService = new CartServiceImpl(Vertx.newInstance(vertx), config(), discovery);
        
        int port = config().getInteger("http.port", PortsConstants.CART_SERVICE_HTTP_PORT);
        String host = config().getString("http.host", "localhost");
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, RxHelper.toFuture(server
                        -> publishHttpEndpoint(MicroServiceNamesConstants.CART_SERVICE_HTTP,
                                host,
                                port,
                                startFuture.completer())));
        
    }
    
    private void addCartEvent(RoutingContext rc, Long customerId) {
        System.out.println("Ide zahtev: " + rc.getBodyAsJson().encodePrettily());
        CartEvent cartEvent = new CartEvent(rc.getBodyAsJson());
        System.out.println("CartEVEntL "+cartEvent);
        if (cartEvent.getCustomerId() == 0) {
            cartEvent.setCustomerId(customerId);
        }
        if (cartEvent.getAmount() > 0 && cartEvent.getCustomerId() == customerId) {
            System.out.println("Dodajem event: " + cartEvent);
            cartService.addCartEvent(cartEvent, ar -> {
                if (ar.succeeded()) {
                    rc.response().setStatusCode(201).end("Event added");
                } else {
                    rc.response().end(ar.cause().toString());
                }
            });
        } else {
            System.out.println("Nesto se ne poklapa");
            System.out.println((cartEvent.getAmount() > 0) + "&&&&" + (cartEvent.getCustomerId() == customerId));
            rc.fail(400);
        }
    }
    
    private void getCart(RoutingContext rc, Long customerId) {
        
        cartService.getCart(customerId, RxHelper.<Cart>toFuture(
                cart -> {
                    System.out.println("Vracam cart: " + cart);
                    rc.response().end(Json.encode(cart));
                },
                cause -> rc.response().setStatusCode(400).end(cause.getMessage())));
    }
    
    private void checkout(RoutingContext rc, Long customerId) {
        cartService.checkout(customerId, RxHelper.<CheckoutResult>toFuture(
                checkout -> rc.response().end(checkout.toJson().encode()),
                cause -> rc.response().setStatusCode(400).end(cause.getMessage())));
    }
    
    protected void requireLogin(RoutingContext context, BiConsumer<RoutingContext, Long> biHandler) {
        Optional<JsonObject> principal = Optional.ofNullable(context.request().getHeader("user-principal"))
                .map(JsonObject::new);
        if (principal.isPresent()) {
            System.out.println("Hello " + principal.get());
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
    private static final String API_GET_CART_TEST = "/cart-test";
}
