/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.cart.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.impl.CompositeFutureImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.RxHelper;
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
import rs.bookstore.cart.Checkout;
import rs.bookstore.cart.event.CartEvent;
import rs.bookstore.cart.repository.impl.CartEventDAOImpl;
import rs.bookstore.cart.service.CartService;
import rs.bookstore.constants.MicroServiceNamesConstants;
import rs.bookstore.cart.repository.CartEventDAO;
import rs.bookstore.order.BookItem;

public class CartServiceImpl implements CartService {

    private final CartEventDAO repository;
    private final ServiceDiscovery discovery;

    public CartServiceImpl(Vertx vertx, JsonObject config, ServiceDiscovery discovery) {
        this.discovery = discovery;
        repository = new CartEventDAOImpl(vertx, config);
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
    public void checkout(Long customerId, Handler<AsyncResult<Checkout>> resultHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
                    service.getBook(bookId, future.completer());
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

        Cart cart = rawCart.setBookItems(currentItems);
        return Future.succeededFuture(cart);
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

}
