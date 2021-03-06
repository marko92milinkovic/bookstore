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
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
//import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import rs.bookstore.constants.MicroServiceNamesConstants;
import static rs.bookstore.constants.MicroServiceNamesConstants.CUSTOMER_SERVICE_RPC;
import rs.bookstore.customer.service.Customer;
import rs.bookstore.customer.service.CustomerService;
import rs.bookstore.lib.MicroServiceVerticle;
import rx.Single;

/**
 *
 * @author markom
 */
public class HttpServerVerticle extends MicroServiceVerticle {

    MongoAuth authProvider;
    //temporary storage
    List<JsonObject> reviews = new ArrayList<>();
    private CircuitBreaker cb_api_book;
    private CircuitBreaker cb_api_cart;
    private CircuitBreaker cb_api_customer;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();


        MongoClient client = MongoClient.createShared(vertx, config());
        JsonObject authProperties = new JsonObject();
        authProvider = MongoAuth.create(client, authProperties);
        authProvider.setCollectionName("user");
        authProvider.setPasswordField("password");
        authProvider.setUsernameField("username");
        authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.EXTERNAL);
        authProvider.setPermissionField("permission");

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
        router.route("/api/bookservice/*").handler(this::dispatchBookRequests);

        // reviews
        router.route("/api/reviews/*").handler(this::dispatchReviewRequest);
        router.route("/eventbus/reviews/*").handler(eventbusReviewsHandler());

        //hystrix dashboard
        // Register the metric handler
        router.get("/hystrix-metrics").handler(HystrixMetricHandler.create(vertx));

        //cart
        router.route("/api/cartservice/*").handler(this::dispatchCartRequests);

        // Serve the static private pages from directory 'private'
        router.route("/private/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("private"));

        // Handles the actual login
        router.post("/loginhandler").handler(FormLoginHandler.create(authProvider)
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
        cb_api_book = CircuitBreaker.create(
                cbConfig.getString("name_cb_api_book", "cb: api-> book"),
                vertx,
                new CircuitBreakerOptions()
                .setMaxFailures(cbConfig.getInteger("max_failures", 3))
                .setFallbackOnFailure(true)
                .setTimeout(cbConfig.getLong("timeout", 3000l))
                .setResetTimeout(cbConfig.getLong("reset_timeout", 7000l)));
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

    private void addToCart(RoutingContext rc) {
        System.out.println(rc.getBodyAsJson().encodePrettily());
        rc.response().end(new JsonObject().put("resp", "ok").encode());
    }

    private void dispatchBookRequests(RoutingContext rc) {
        System.out.println("Dispatch book request");
        cb_api_book.executeWithFallback(cbFuture -> {
            discovery.getRecord(record
                    -> record.getType().equals(HttpEndpoint.TYPE)
                    && record.getName().equals(MicroServiceNamesConstants.BOOK_SERVICE_HTTP), ar -> {

                if (ar.succeeded() && ar.result() != null) {

                    Record bookMicroserviceRecord = ar.result();
                    WebClient webClient
                            = discovery.getReference(bookMicroserviceRecord).getAs(WebClient.class);
                    String apiPAth = (rc.request().uri().split("/api/bookservice"))[1];
                    Single<HttpResponse<Buffer>> singleResponse;
                    System.out.println("Body: " + rc.getBodyAsString());
                    if (rc.getBody() == null) {
                        singleResponse = webClient.request(rc.request().method(), apiPAth)
                                .rxSend();
                    } else {
                        singleResponse = webClient.request(rc.request().method(), apiPAth)
                                .rxSendBuffer(new Buffer(rc.getBody()));
                    }
                    singleResponse.subscribe(
                            response -> {
                                if (response.statusCode() >= 500) {
                                    cbFuture.fail(response.statusCode() + ": " + response.toString());
                                } else {
                                    HttpServerResponse toRsp = rc.response()
                                    .setStatusCode(response.statusCode());
                                    response.headers().getDelegate().forEach(header -> {
                                        toRsp.putHeader(header.getKey(), header.getValue());
                                    });
                                    System.out.println("Body: " + response.bodyAsString());
                                    toRsp.end(response.bodyAsString());
                                    cbFuture.complete();
                                }
                            }, cause -> {
                                if (!rc.response().ended()) {
                                    rc.response().end(cause.getMessage());
                                }
                                cbFuture.fail(cause);
                            });
                } else {
                    rc.response().end("Http microservices not found");
                    cbFuture.complete();
                    if (ar.cause() != null) {
                        ar.cause().printStackTrace();
                    }
                }
            }
            );
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

    private void dispatchCartRequests(RoutingContext rc) {
        System.out.println("Hocu da dispatcujem carts");

        User user = rc.user();
        if (user == null) {
            rc.response().setStatusCode(401).end("not logged in");
            return;
        }

        cb_api_cart.executeWithFallback(cbFuture -> {
            discovery.getRecord(record
                    -> record.getType().equals(HttpEndpoint.TYPE)
                    && record.getName().equals(MicroServiceNamesConstants.CART_SERVICE_HTTP),
                    RxHelper.toFuture(
                            record -> {
                                HttpClient httpClient
                                = discovery.getReference(record).get();
                                String apiPAth = (rc.request().uri().split("/api/cartservice"))[1];

                                HttpClientRequest req = httpClient.request(rc.request().method(), apiPAth, response -> {
                                    response.bodyHandler(body -> {
                                        if (response.statusCode() >= 500) {
                                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                                        } else {
                                            HttpServerResponse toRsp = rc.response()
                                                    .setStatusCode(response.statusCode());
                                            response.headers().forEach(header -> {
                                                toRsp.putHeader(header.getKey(), header.getValue());
                                            });

                                            System.out.println("RESP is :" + body);
                                            toRsp.end(body);
                                            cbFuture.complete();
                                        }
                                    });
                                });
                                if (rc.user() != null) {
                                    System.out.println("Dodajem user: " + rc.user());
                                    req.putHeader("user-principal", rc.user().principal().encode());
                                }
                                System.out.println("Call cart-microservice on url: " + req.uri());

                                if (rc.getBody() != null) {
                                    System.out.println("Dodajem body: " + rc.getBodyAsString());
                                    req.end(rc.getBody());
                                } else {
                                    req.end();
                                }
                            }, cause -> {
                                cause.printStackTrace();
                                if (!rc.response().ended()) {
                                    rc.response().end("Http microservices not found");
                                }
                                cbFuture.complete();
                            })
            );
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
            Future<CustomerService> futureCustomerService = Future.future();
            EventBusService.getServiceProxyWithJsonFilter(
                    discovery,
                    new JsonObject().put("name", CUSTOMER_SERVICE_RPC),
                    CustomerService.class,
                    futureCustomerService.completer());
            cb_api_customer.executeWithFallback(cbFuture -> {

                futureCustomerService.compose(customerService -> {
                    Future<Customer> customerFuture = Future.future();
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

    //TO DO: refactor
    private <T> Handler<AsyncResult<T>> resultHandlerNonEmpty(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                T res = ar.result();
                if (res == null) {
                    context.response().end("NOT found....see line 279");
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(res.toString());
                }
            } else {
                context.response().end(ar.cause().getMessage());
                ar.cause().printStackTrace();
            }
        };
    }

    private SockJSHandler eventbusReviewsHandler() {
        BridgeOptions bridgeOptions = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("book\\.reviews\\.[0-9]+"));

        return SockJSHandler.create(vertx).bridge(bridgeOptions);
    }
}
