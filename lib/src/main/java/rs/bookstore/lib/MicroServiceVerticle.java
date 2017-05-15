package rs.bookstore.lib;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.CompositeFutureImpl;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.JDBCDataSource;
import io.vertx.servicediscovery.types.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 *
 * @author marko
 */
public abstract class MicroServiceVerticle extends AbstractVerticle {

    protected ServiceDiscovery discovery;
    //Records need to unpublish when the verticle is undeployed
    protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

    //initialization for the discovery and circuitBreaker
    @Override
    public void start() {
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));
    }

    //Several helper methods to publish various kind of services
    public void publishHttpEndpoint(String name, String host, int port, Handler<AsyncResult<Void>> completionHandler) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/");
        publish(record, completionHandler);
    }

    public void publishMessageSource(String name, String address, Class contentClass, Handler<AsyncResult<Void>> completionHandler) {
        Record record = MessageSource.createRecord(name, address, contentClass);
        publish(record, completionHandler);
    }

    public void publishMessageSource(String name, String address, Handler<AsyncResult<Void>> completionHandler) {
        Record record = MessageSource.createRecord(name, address);
        publish(record, completionHandler);
    }

    protected void publishJDBCDataSource(String name, JsonObject location, Handler<AsyncResult<Void>> completionHandler) {
        Record record = JDBCDataSource.createRecord(name, location, new JsonObject());
        publish(record, completionHandler);
    }

    public void publishEventBusService(String name, String address, Class serviceClass, Handler<AsyncResult<Void>> completionHandler) {
        Record record = EventBusService.createRecord(name, address, serviceClass);
        publish(record, completionHandler);
    }

    //A record represent a service. Service types are distinguished by the type field in the record
    //Every service types provided by Vert.x contains several createRecord methods.
    //We need to give every service a proper name so that we can retrieve it by name
    //OBRATI PAZNJU na meta data za API gateway
    private void publish(Record record, Handler<AsyncResult<Void>> completionHandler) {
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                registeredRecords.add(record);
//                logger.info("Service "+ar.result().getName()+" published successfuly");
                completionHandler.handle(Future.succeededFuture());
            } else {
                completionHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    protected Future<HttpServer> createHttpServer(Router router, String host, int port) {
        Future<HttpServer> future = Future.future();
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(port, host, future.completer());
        return future;
    }

    //unpublish the service records when the veticle is undeployed
    @Override
    public void stop(Future<Void> future) throws Exception {
        List<Future> futures = new ArrayList<>();
        for (Record record : registeredRecords) {
            Future<Void> unregistrationFuture = Future.future();
            futures.add(unregistrationFuture);
            discovery.unpublish(record.getRegistration(), unregistrationFuture.completer());
        }
        if (futures.isEmpty()) {
            discovery.close();
            future.complete();
        } else {
            //We invoke CompositeFuture.all(futures) to fold all futures
            //We set a handler to the composite future and only if every unpublished 
            //result is successful, the discovery can be closed directly or the stop
            //procedure will fail
            CompositeFuture.all(futures)
                    .setHandler(ar -> {
                        discovery.close();
                        if (ar.failed()) {
                            future.fail(ar.cause());
                        } else {
                            future.complete();
                        }
                    });
        }
    }
}
