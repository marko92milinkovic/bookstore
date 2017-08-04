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
import rs.bookstore.cart.Cart;
import rs.bookstore.order.CheckoutResult;
import rs.bookstore.cart.event.CartEvent;
import rs.bookstore.cart.repository.impl.CartEventDAOImpl;
import rs.bookstore.cart.service.CartService;
import rs.bookstore.constants.MicroServiceNamesConstants;
import rs.bookstore.cart.repository.CartEventDAO;
import rs.bookstore.order.BookItem;
import rs.bookstore.order.Order;

public class CartServiceImpl implements CartService {
    
    private final CartEventDAO repository;
    private final ServiceDiscovery discovery;
    private final CircuitBreaker cb_book;
    private final CircuitBreaker cb_inventory;
    private final Vertx vertx;
    
    public CartServiceImpl(Vertx vertx, JsonObject config, ServiceDiscovery discovery) {
        this.discovery = discovery;
        this.vertx = vertx;
        repository = new CartEventDAOImpl(vertx, config);
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
                .takeWhile(cartEvent -> !CartEvent.isTerminal(cartEvent.getCartEventType()))
                .reduce(new Cart(), Cart::incorporate)
                .toSingle()
                .subscribe(future::complete, future::fail);
        
        future.compose(cart
                -> getBookService()
                        .compose(service -> prepareBook(service, cart)) // prepare book data
                        .compose(bookList -> generateCurrentCartFromStream(cart, bookList)) // prepare book items
        ).setHandler(resultHandler);
    }
    
    @Override
    public void checkout(Long customerId, Handler<AsyncResult<CheckoutResult>> resultHandler) {
        System.out.println("Start checkout");
        if (customerId == null) {
            resultHandler.handle(Future.failedFuture(new IllegalStateException("Invalid customer")));
            return;
        }
        Future<Cart> cartFuture = getCurrentCart(customerId);
        Future<CheckoutResult> orderFuture = cartFuture.compose(cart
                -> checkAvailableInventory(cart).compose(checkResult -> {
                    if (checkResult.getBoolean("res")) {
                        double totalPrice = calculateTotalPrice(cart);
                        // create order instance
                        Order order = new Order();
                        order.setCustomerId(customerId);
                        order.setPaymentId(-99);
                        order.setTotalPrice(totalPrice);
                        order.setBookItems(cart.getBookItems());
                        // set id and then send order, wait for reply
                        return sendOrderAwaitResult(order)
                                .compose(result -> saveCheckoutEvent(customerId).map(v -> result));
                    } else {
                        // has insufficient inventory, fail
                        return Future.succeededFuture(new CheckoutResult()
                                .setResultMessage(checkResult.getString("message")));
                    }
                })
        );
        
        orderFuture.setHandler(resultHandler);
    }
    
    private void retrieveBook() {
        discovery.getRecord(record
                -> record.getType().equals(HttpEndpoint.TYPE)
                && record.getName().equals(MicroServiceNamesConstants.BOOK_SERVICE_RPC),
                RxHelper.toFuture(record -> {
                    HttpClient httpClient
                            = discovery.getReference(record).get();
                }, cause -> {
                }));
    }

    /**
     * Prepare meta book data stream for shopping cart.
     *
     * @param service book service instance
     * @param cart raw shopping cart instance
     * @return async result
     */
    private Future<List<Book>> prepareBook(BookService service, Cart cart) {
        List<Future<Book>> futures = cart.getAmountMap().keySet()
                .stream()
                .map(bookId -> {
                    Future<Book> future = Future.future();
                    cb_book.execute(cbFuture -> {
                        System.out.println("Saljem zahtev");
                        service.getBook(bookId, RxHelper.toFuture(
                                book -> {
                                    cbFuture.complete();
                                    if (!future.isComplete()) {
                                        future.complete(book);
                                    }
                                }, cause -> {
                                    System.out.println("Problem");
                                    cbFuture.fail(cause);
                                }));
                    }).setHandler(ar -> {
                        if (ar.failed()) {
                            System.out.println("CIRCUIT - BREAKER JE PUKAO jer " + ar.cause().getMessage());
                            future.fail(ar.cause());
                        } else {
                            System.out.println("REZULTAT: " + ar.result());
                        }
                    });
                    return future;
                })
                .collect(Collectors.toList());

        /**
         * Evaluate a list of futures. Transforms a `List[Future[R]]` into a
         * `Future[List[R]]`.
         * <p>
         * When all futures succeed, the result future completes with the list
         * of each result of elements in {@code futures}.
         * </p>
         * The returned future fails as soon as one of the futures in
         * {@code futures} fails. When the list is empty, the returned future
         * will be already completed.
         * <p>
         * Useful for reducing many futures into a single @{link Future}.
         *
         * @param futures a list of {@link Future futures}
         * @return the transformed future
         */
        return CompositeFutureImpl.all(futures.toArray(new Future[futures.size()]))
                .map(v -> futures.stream()
                .map(Future::result)
                .collect(Collectors.toList())
                );
    }

    /**
     * Generate current shopping cart from a data stream including necessary
     * book data. Note: this is not an asynchronous method. `Future` only marks
     * whether the process is successful.
     *
     * @param rawCart raw shopping cart
     * @param bookList book data stream
     * @return async result
     */
    private Future<Cart> generateCurrentCartFromStream(Cart rawCart, List<Book> bookList) {
        Future<Cart> future = Future.future();
        // check if any of the book is invalid
        if (bookList.stream().anyMatch(Objects::isNull)) {
            future.fail("Error when retrieve books: empty");
            return future;
        }
        // construct the book items
        List<BookItem> currentItems = rawCart.getAmountMap().entrySet()
                .stream()
                .map(item -> new BookItem(getBookFromStream(bookList, item.getKey()),
                item.getValue()))
                .filter(item -> item.getAmount() > 0)
                .collect(Collectors.toList());
        rawCart.setBookItems(currentItems);
        return Future.succeededFuture(rawCart);
    }
    
    private Book getBookFromStream(List<Book> bookList, Long bookId) {
        return bookList.stream()
                .filter(book -> book.getBookId() == bookId)
                .findFirst()
                .get();
    }

    /**
     * Get book service from the service discovery infrastructure.
     *
     * @return async result of the service.
     */
    private Future<BookService> getBookService() {
        Future<BookService> future = Future.future();
        EventBusService.getProxy(discovery, BookService.class, future.completer());
        return future;
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
    
    private Future<Cart> getCurrentCart(Long customerId) {
        Future<Cart> cartFuture = Future.future();
        getCart(customerId, cartFuture.completer());
        return cartFuture.compose(c -> {
            if (c == null || c.getBookItems().isEmpty()) {
                return Future.failedFuture(new IllegalStateException("Invalid shopping cart"));
            } else {
                return Future.succeededFuture(c);
            }
        });
    }
    
    private double calculateTotalPrice(Cart cart) {
        return cart.getBookItems().stream()
                .map(p -> p.getAmount() * p.getPrice()) // join by book id
                .reduce(0.0d, (a, b) -> a + b);
    }
    
    private Future<HttpClient> getInventoryEndpoint() {
        Future<HttpClient> future = Future.future();
        HttpEndpoint.getClient(discovery,
                new JsonObject().put("name", MicroServiceNamesConstants.INVENTORY_SERVICE),
                future.completer());
        return future;
    }
    
    private Future<JsonObject> getInventory(BookItem item, HttpClient client) {
        Future<Integer> future = Future.future();
        cb_inventory.executeWithFallback(cbFuture -> {
            client.get("/inventory/" + item.getBookId(), response -> {
                if (response.statusCode() == 200) {
                    response.bodyHandler(buffer -> {
                        try {
                            int inventory = Integer.valueOf(buffer.toString());
                            future.complete(inventory);
                        } catch (NumberFormatException ex) {
                            future.fail(ex);
                        }
                    });
                } else {
                    future.fail("not_found:" + item.getBookId());
                }
            })
                    .exceptionHandler(future::fail)
                    .end();
        }, Throwable::getMessage).setHandler(ar -> {
            if (ar.failed()) {
                future.fail(ar.cause());
            }
        });
        
        return future.map(inv -> new JsonObject()
                .put("id", item.getBookId())
                .put("inventory", inv)
                .put("amount", item.getAmount()));
    }

    /**
     * Check inventory for the current cart.
     *
     * @param cart shopping cart data object
     * @return async result
     */
    private Future<JsonObject> checkAvailableInventory(Cart cart) {
        Future<List<JsonObject>> allInventories = getInventoryEndpoint().compose(client -> {
            List<Future<JsonObject>> futures = cart.getBookItems()
                    .stream()
                    .map(book -> getInventory(book, client))
                    .collect(Collectors.toList());
            return CompositeFutureImpl.all(futures.toArray(new Future[futures.size()]))
                    .map(v -> futures.stream()
                    .map(Future::result)
                    .collect(Collectors.toList())
                    )
                    .map(r -> {
                        ServiceDiscovery.releaseServiceObject(discovery, client);
                        return r;
                    });
        });
        return allInventories.map(inventories -> {
            JsonObject result = new JsonObject();
            // get the list of books whose inventory is lower than the demand amount
            List<JsonObject> insufficient = inventories.stream()
                    .filter(item -> item.getInteger("inventory") - item.getInteger("amount") < 0)
                    .collect(Collectors.toList());
            // insufficient inventory exists
            if (insufficient.size() > 0) {
                System.out.println("insufficient inventory exists");
                String insufficientList = insufficient.stream()
                        .map(item -> item.getString("id"))
                        .collect(Collectors.joining(", "));
                result.put("message", String.format("Insufficient inventory available for book %s.", insufficientList))
                        .put("res", false);
            } else {
                System.out.println("OK amount");
                result.put("res", true);
            }
            return result;
        });
    }

    /**
     * Save checkout cart event for current customer.
     *
     * @param customerId customer id
     * @return async result
     */
    private Future<Void> saveCheckoutEvent(Long customerId) {
        Future<Void> resFuture = Future.future();
        CartEvent event = CartEvent.createCheckoutEvent(customerId);
        addCartEvent(event, resFuture.completer());
        return resFuture;
    }
    
    String PAYMENT_EVENT_ADDRESS = "events.service.shopping.to.payment";
    String ORDER_EVENT_ADDRESS = "events.service.shopping.to.order";
}
