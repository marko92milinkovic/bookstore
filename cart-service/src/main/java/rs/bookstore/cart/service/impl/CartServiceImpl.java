/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart.service.impl;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.impl.CompositeFutureImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava.core.Vertx;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import rs.bookstore.book.domain.Book;
import rs.bookstore.book.service.BookService;
import rs.bookstore.cart.domain.Cart;
import rs.bookstore.cart.repository.CartDAO;
import rs.bookstore.cart.repository.impl.CartDAOImpl;
import rs.bookstore.order.CheckoutResult;
import rs.bookstore.cart.domain.CartEvent;
import rs.bookstore.cart.repository.impl.CartEventDAOImpl;
import rs.bookstore.cart.service.CartService;
import rs.bookstore.constants.MicroServiceNamesConstants;
import rs.bookstore.cart.repository.CartEventDAO;
import rs.bookstore.cart.domain.CartItem;
import rs.bookstore.order.Order;
import rx.Single;

public class CartServiceImpl implements CartService {
    
    private final CartEventDAO repository;
    private final CartDAO cartRepository;
    private final ServiceDiscovery discovery;
    private final CircuitBreaker cb_book;
    private final CircuitBreaker cb_inventory;
    private final Vertx vertx;
    
    public CartServiceImpl(Vertx vertx, JsonObject config, ServiceDiscovery discovery) {
        this.discovery = discovery;
        this.vertx = vertx;
        repository = new CartEventDAOImpl(vertx, config);
        cartRepository = new CartDAOImpl(vertx, config);
        // init circuit breaker instance
        JsonObject cbOptions = config.getJsonObject("circuit-breaker") != null
                ? config.getJsonObject("circuit-breaker") : new JsonObject();
        cb_book = CircuitBreaker.create(cbOptions.getString("name", "cb: cart->book"), vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbOptions.getInteger("max-failures", 3))
                        .setTimeout(cbOptions.getLong("timeout", 4000L))
                        .setFallbackOnFailure(true)
                        .setResetTimeout(cbOptions.getLong("reset-timeout", 10000L)));
        
        cb_inventory = CircuitBreaker.create(cbOptions.getString("name", "cb: cart->book"), vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbOptions.getInteger("max-failures", 3))
                        .setTimeout(cbOptions.getLong("timeout", 4000L))
                        .setFallbackOnFailure(true)
                        .setResetTimeout(cbOptions.getLong("reset-timeout", 10000L)));
    }
    
    @Override
    public void addCartEvent(CartEvent event, Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> future = Future.future();
        repository.addOne(event).subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
    }
    
    @Override
    public void getCart(Long customerId, Handler<AsyncResult<Cart>> resultHandler) {
        Future<Cart> future = Future.future();
        // aggregate cart events into raw shopping cart
        repository.streamByCustomerId(customerId)
                .takeWhile(cartEvent ->
                        !CartEvent.isTerminal(cartEvent.getCartEventType()))
                .reduce(new Cart(customerId), Cart::addEvent)
                .toSingle()
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
    }

    @Override
    public void checkout(Long customerId, Handler<AsyncResult<CheckoutResult>> resultHandler) {
        System.out.println("Start checkout");
        if (customerId == null) {
            resultHandler.handle(Future.failedFuture(new IllegalStateException("Invalid customer")));
            return;
        }
        Future<Cart> retrieveCart = Future.future();
        getCart(customerId, retrieveCart.completer());
        retrieveCart.map(cart-> saveCheckoutEvent(customerId));


//        Future<Cart> cartFuture = (customerId);
//        Future<CheckoutResult> orderFuture = cartFuture.compose(cart
//                -> checkAvailableInventory(cart).compose(checkResult -> {
//                    if (checkResult.getBoolean("res")) {
//                        double totalPrice = calculateTotalPrice(cart);
//                        // create order instance
//                        Order order = new Order();
//                        order.setCustomerId(customerId);
//                        order.setPaymentId(-99);
//                        order.setTotalPrice(totalPrice);
////                        order.setBookItems(cart.getBookItems());
//                        // set id and then send order, wait for reply
//                        return sendOrderAwaitResult(order)
//                                .compose(result -> saveCheckoutEvent(customerId).map(v -> result));
//                    } else {
//                        // has insufficient inventory, fail
//                        return Future.succeededFuture(new CheckoutResult()
//                                .setResultMessage(checkResult.getString("message")));
//                    }
//                })
//        );
        
//        orderFuture.setHandler(resultHandler);
    }

    @Override
    public void saveCart(Cart cart, Handler <AsyncResult <Void>> resultHandler) {
        cartRepository.addOne(cart).subscribe(RxHelper.toSubscriber(resultHandler));
    }


    /**
     * Send the order to the order microservice and wait for reply.
     *
     * @param order order data object
     * @return async result
     */
    private Future<CheckoutResult> sendOrderAwaitResult(Order order) {
        Future<CheckoutResult> future = Future.future();
        vertx.eventBus().send(ORDER_EVENT_ADDRESS, order.toJson(), reply -> {
            if (reply.succeeded()) {
                future.complete(new CheckoutResult((JsonObject) reply.result().body()));
            } else {
                future.fail(reply.cause());
            }
        });
        return future;
    }
    

    
    private double calculateTotalPrice(Cart cart) {
        return 0;
//        return cart.getCartItems().stream()
//                .map(p -> p.getAmount() * p.getPrice()) // join by book id
//                .reduce(0.0d, (a, b) -> a + b);
    }

    private Future<Void> saveCheckoutEvent(Long customerId) {
        Future<Void> resFuture = Future.future();
        CartEvent event = CartEvent.createCheckoutEvent(customerId);
        addCartEvent(event, resFuture.completer());
        return resFuture;
    }
    
    String PAYMENT_EVENT_ADDRESS = "events.service.shopping.to.payment";
    String ORDER_EVENT_ADDRESS = "events.service.shopping.to.order";
}
