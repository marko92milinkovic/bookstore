/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.apigateway;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.circuitbreaker.HystrixMetricHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.client.HttpRequest;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.EventBusService;
import rs.bookstore.book.domain.Book;
import rs.bookstore.constants.MicroServiceNamesConstants;
import rs.bookstore.customer.service.Customer;
import rs.bookstore.customer.service.CustomerService;
import rs.bookstore.lib.MicroServiceVerticle;
import rx.Observable;
import rx.Single;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

import static rs.bookstore.constants.MicroServiceNamesConstants.BOOK_SERVICE_HTTP;
import static rs.bookstore.constants.MicroServiceNamesConstants.CUSTOMER_SERVICE_RPC;

//import io.vertx.ext.web.handler.FormLoginHandler;

/**
 * @author markom
 */
public class HttpServerVerticle extends MicroServiceVerticle {

    MongoAuth authProvider;
    //temporary storage
    List <JsonObject> reviews = new ArrayList <>();
    private CircuitBreaker cb_api_cart;
    private CircuitBreaker cb_api_customer;

    @Override
    public void start(Future <Void> startFuture) throws Exception {
        super.start();

        JsonObject authProperties = new JsonObject();

        MongoClient client = MongoClient.createShared(vertx, config().getJsonObject("mongo"));
        authProvider = MongoAuth.create(client, authProperties);
        authProvider.setCollectionName("user")
                .setPasswordField("password")
                .setUsernameField("username")
                .setPermissionField("permission")
                .getHashStrategy().setSaltStyle(HashSaltStyle.EXTERNAL);

        Router router = Router.router(vertx);

        // We need cookies, sessions and request bodies
        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx, "shopping.session")));

        // We need a user session handler too to make sure the user is stored in the session between requests
        router.route().handler(UserSessionHandler.create(authProvider));

        // Any requests to URI starting '/private/' require login
        router.route("/private/*").handler(RedirectAuthHandler.create(authProvider, "/view/login.html"));
        // Any requests to URI starting '/api/' require login
//        ---------------------------OTKOMENTARISATI---------------
//        router.route("/auth/*").handler(RedirectAuthHandler.create(authProvider, "/view/login.html"));

        // book api dispatcher
//        router.route("/api/bookservice/*").handler(rc -> dispatchHttpServiceRequest(rc, cb_api_book,
//                MicroServiceNamesConstants.BOOK_SERVICE_HTTP));
        router.route("/api/bookservice/*").handler(this::dispatchBookRequests);
        router.post("/customer/create").handler(this::createCustomer);
        // reviews
        router.route("/api/reviews/*").handler(this::dispatchReviewRequest);
        router.route("/eventbus/reviews/*").handler(eventbusReviewsHandler());
        //hystrix dashboard
        // Register the metric handler
        router.get("/hystrix-metrics").handler(HystrixMetricHandler.create(vertx));

        //cart

        router.route("/api/cartservice/*")
                .handler(RedirectAuthHandler.create(authProvider, "/view/login.html"));
        router.route("api/cartservice/cart").handler(this::getCart);


        // Serve the static private pages from directory 'private'
        router.route("/private/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("private"));

        // Handles the actual login
        router.post("/loginhandler").handler(
                FormLoginHandler.create(authProvider)
                        .setUsernameParam("username")
                        .setPasswordParam("password")
                        .setDirectLoggedInOKURL("/#!/account")
                        .setReturnURLParam("/loginFailed"));

        router.route("/auth/customer/get").handler(this::getCustomerCredentials);

        // Implement logout
        router.route("/logout").handler(context -> {
            context.clearUser();
            // Redirect back to the index page
            context.response().putHeader("location", "/").setStatusCode(302).end();
        });

        router.post("/api/cart/events").handler(this::addToCart);

        // Serve the non private static pages
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(8080, ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
        //init circuit breakers:
        JsonObject cbConfig = config()
                .getJsonObject("circuit_breaker") != null
                ? config().getJsonObject("circuit-breaker") : new JsonObject();
        cb_api_cart = CircuitBreaker.create(
                cbConfig.getString("name_cb_api_cart", "cb: api-> cart"),
                vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbConfig.getInteger("max_failures", 3))
                        .setFallbackOnFailure(true)
                        .setTimeout(cbConfig.getLong("timeout", 3000l))
                        .setResetTimeout(cbConfig.getLong("reset_timeout", 7000l)));
        cb_api_customer = CircuitBreaker.create(
                cbConfig.getString("name_cb_api_cart", "cb: api-> customer"),
                vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbConfig.getInteger("max_failures", 3))
                        .setFallbackOnFailure(true)
                        .setTimeout(cbConfig.getLong("timeout", 3000l))
                        .setResetTimeout(cbConfig.getLong("reset_timeout", 7000l)));


    }

    private <T> Observable <T> dispatchBookRequests(Class <T> resultClass, String apiPath) {
        BooksHystrixCommand command = new BooksHystrixCommand("book", () ->
                retrieveWebClient(BOOK_SERVICE_HTTP)
                        .flatMap(webClient -> webClient.get(apiPath).rxSend())
                        .map(body -> body.bodyAsJson(resultClass))
                        .toObservable());

        return command
                .construct()
                .subscribeOn(RxHelper.scheduler(context));
    }

    private void getCart(RoutingContext rc) {
        dispatchHttpServiceRequest(rc, cb_api_cart, MicroServiceNamesConstants.CART_SERVICE_HTTP);
    }



    private void createCustomer(RoutingContext rc) {
        Future <CustomerService> futureCustomerService = Future.future();
        EventBusService.getServiceProxyWithJsonFilter(
                discovery,
                new JsonObject().put("name", CUSTOMER_SERVICE_RPC),
                CustomerService.class,
                futureCustomerService.completer());

        String username = rc.request().getParam("username");
        String password = rc.request().getParam("password");
        if (username.isEmpty() || password.isEmpty()) {
            rc.response().end("username and password are mandatory");
            return;
        }

        futureCustomerService.compose(service -> {
            Future <Customer> createdCustomer = Future.future();
            service.createNewCustomer(new Customer(rc.getBodyAsJson()), createdCustomer.completer());
            return createdCustomer;
        }).compose(customer -> {
            Future <String> insert = Future.future();
            authProvider.insertUser(username, password,
                    Arrays.asList("customer"), Arrays.asList("customer"), insert.completer());
            return insert;
        }).setHandler(RxHelper.toFuture(
                result -> rc.response().end(new JsonObject().put("status", result).encode()),
                error -> rc.response().end("error", error.getMessage())
        ));
    }

    private void addToCart(RoutingContext rc) {
        System.out.println(rc.getBodyAsJson().encodePrettily());
        rc.response().end(new JsonObject().put("resp", "ok").encode());
    }

    private void dispatchBookRequests(RoutingContext rc) {

    }

    private void getCustomerCredentials(RoutingContext rc) {
        if (rc.user() == null) {
            rc.response().setStatusCode(401).end("No user authenticated");
            return;
        }

        JsonObject principal = rc.user().principal();
        System.out.println("Principal is: " + principal);
        String username = principal.getString("username");
        Long customer_id = principal.getLong("customer_id");

        if (username == null || customer_id == null) {
            rc.response()
                    .putHeader("content-type", "application/json")
                    .end(new Customer(
                            new JsonObject()
                                    .put("username", "testUsername")
                                    .put("id", "testId")).toJson().encodePrettily());
        } else {
            Future <CustomerService> futureCustomerService = Future.future();
            EventBusService.getServiceProxyWithJsonFilter(
                    discovery,
                    new JsonObject().put("name", CUSTOMER_SERVICE_RPC),
                    CustomerService.class,
                    futureCustomerService.completer());
            cb_api_customer.executeWithFallback(cbFuture -> {

                futureCustomerService.compose(customerService -> {
                    Future <Customer> customerFuture = Future.future();
                    customerService.getCustomerByUsername(username, customerFuture.completer());
                    return customerFuture;
                }).setHandler(ar -> {
                    if (ar.succeeded()) {
                        Customer res = ar.result();
                        System.out.println("sending customer...... " + res);
                        if (res == null) {
                            rc.response().end("NOT found....see line 137");
                        } else {
                            rc.response()
                                    .putHeader("content-type", "application/json")
                                    .end(res.toJson().encodePrettily());
                        }
                        cbFuture.complete();
                    } else {
                        cbFuture.fail(ar.cause());
                        if (!rc.response().ended()) {
                            rc.response().end(ar.cause().getMessage());
                        }
                        ar.cause().printStackTrace();
                    }
                });
            }, Throwable::getMessage).setHandler(ar -> {
                if (ar.failed()) {
                    ar.cause().printStackTrace();
                    rc.response()
                            .setStatusCode(502)
                            .putHeader("content-type", "application/json")
                            .end(new JsonObject().put("error", "bad_gateway")
                                    //.put("message", ex.getMessage())
                                    .encodePrettily());
                }
            });

        }
    }

    private void dispatchReviewRequest(RoutingContext rc) {
        User user = rc.user();

//        if(user==null) {
//            rc.response().setStatusCode(401).end("not logged in");
//            return;
//        }
        int creatorId = user.principal().getInteger("_id");
        String comment = rc.request().getParam("comment");
        int rate = Integer.parseInt(rc.request().getParam("rate"));
        int bookId = Integer.parseInt(rc.request().getParam("bookId"));

        //send comment to review-service
        JsonObject review = new JsonObject()
                .put("comment", comment)
                .put("rate", rate)
                .put("bookId", bookId)
                .put("creatorId", creatorId);
        reviews.add(review);

        OptionalDouble avgRateOptional = reviews.stream().mapToInt(r -> r.getInteger("rate")).average();
        double avgRate = rate;
        if (avgRateOptional.isPresent()) {
            avgRate = avgRateOptional.getAsDouble();
        }

        rc.vertx().eventBus().publish("book.reviews." + bookId, new JsonObject().put("review", review).put("avgRate", avgRate));
        rc.response().end("ok :) ");
    }

    private SockJSHandler eventbusReviewsHandler() {
        BridgeOptions bridgeOptions = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("book\\.reviews\\.[0-9]+"));

        return SockJSHandler.create(vertx).bridge(bridgeOptions);
    }


    public void dispatchHttpServiceRequest(RoutingContext rc, CircuitBreaker cb, String serviceName) {
        HttpServerResponse response = rc.response();
        String apiPath = "/" + (rc.request().uri().split("/api/.+/"))[1];
        System.out.println("Api path: " + apiPath);

        cb.executeWithFallback(operation -> {
                    retrieveWebClient(serviceName)
                            .flatMap(webClient -> {
                                HttpRequest <Buffer> request = webClient.request(rc.request().method(), apiPath);
                                if (rc.user() != null) {
                                    request.putHeader("user-principal", rc.user().principal().encode());
                                }

                                if (rc.getBody() == null) {
                                    return request.rxSend();
                                } else {
                                    return request.rxSendBuffer(new Buffer(rc.getBody()));
                                }
                            })
                            .subscribe(serviceResp -> {
                                        response.headers().addAll(serviceResp.headers().getDelegate());
                                        operation.complete(serviceResp.bodyAsBuffer().toString());
                                    },
                                    operation::fail
                            );
                }, error -> new JsonObject().encode()
        ).setHandler(ar -> {
            if (ar.failed()) {
                response.end(new JsonObject().encode());
            } else {
                response.end(ar.result());
            }
        });
    }

    private Single <Book> getBook() {
        WebClient client = null;

        int bookId = 5;
        return client.get("api/get/" + bookId)
                .timeout(5000)
                .rxSend()
                .map(response -> response.bodyAsJson(Book.class));
    }

    private Future <CustomerService> retrieveCustomerService() {
        Future <CustomerService> customerServiceFuture = Future.future();
        EventBusService.getServiceProxyWithJsonFilter(
                discovery,
                new JsonObject().put("name", CUSTOMER_SERVICE_RPC),
                CustomerService.class,
                customerServiceFuture.completer());

        return customerServiceFuture;
    }

    private Single <WebClient> retrieveWebClient(String serviceName) {
        io.vertx.rxjava.core.Future <Record> recordFuture = io.vertx.rxjava.core.Future.future();
        discovery.getRecord(record -> record.getName().equals(serviceName),
                recordFuture.completer());
        return recordFuture
                .map(record -> discovery.getReference(record).getAs(WebClient.class))
                .rxSetHandler();
    }

    private void dispatchBookRequestusingHystrix(RoutingContext rc) {
        String apiPath = "/" + (rc.request().uri().split("/api/.+service/"))[1];
        System.out.println("Api path: " + apiPath);

        BooksHystrixCommand command = new BooksHystrixCommand("book", () ->
                retrieveWebClient(BOOK_SERVICE_HTTP)
                        .flatMap(webClient -> webClient.get(apiPath).rxSend())
                        .map(body -> body.bodyAsString())
                        .toObservable());

        System.out.println("Deployment: " + context.deploymentID());

        command.observe()
                .subscribe(result -> {
                    System.out.println("Deployment: " + context.deploymentID());
                    context.runOnContext(v -> {
                        System.out.println("Thread is: " + Thread.currentThread().getName());
                        rc.response().end(String.valueOf(result));
                    });
                }, error -> {
                    rc.response().end(new JsonObject().encode());
                });
    }


}
