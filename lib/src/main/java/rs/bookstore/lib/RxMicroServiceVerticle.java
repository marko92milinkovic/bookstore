/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.lib;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava.servicediscovery.types.EventBusService;
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint;
import io.vertx.rxjava.servicediscovery.types.JDBCDataSource;
import io.vertx.rxjava.servicediscovery.types.MessageSource;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import java.util.Set;
import rx.Single;

/**
 *
 * @author markom
 */
public abstract class RxMicroServiceVerticle extends AbstractVerticle {

    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;
    protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

    @Override
    public void start() throws Exception {
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

        JsonObject cbConfig = config()
                .getJsonObject("circuit_breaker") != null
                ? config().getJsonObject("circuit-breaker") : new JsonObject();
        circuitBreaker = CircuitBreaker.create(
                cbConfig.getString("name", "cb"),
                vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbConfig.getInteger("max_failures", 3))
                        .setFallbackOnFailure(true)
                        .setTimeout(cbConfig.getLong("timeout", 5000l))
                        .setResetTimeout(cbConfig.getLong("reset_timeout", 20000l)));
    }

    public Single<Void> publishHttpEndpoint(String name, String host, int port) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/");
        Single<Void> publish = publish(record);
        publish.subscribe(v -> System.out.println(" Http record added " + v), System.err::println);
        return publish;
    }

    public Single<Void> publishMessageSource(String name, String address, Class contentClass) {
        Record record = MessageSource.createRecord(name, address, contentClass.getName());
        return publish(record);
    }

    public Single<Void> publishMessageSource(String name, String address) {
        Record record = MessageSource.createRecord(name, address);
        return publish(record);
    }

    protected Single<Void> publishJDBCDataSource(String name, JsonObject location) {
        Record record = JDBCDataSource.createRecord(name, location, new JsonObject());
        return publish(record);
    }

    public Single<Void> publishEventBusService(String name, String address, Class serviceClass) {
        Record record = EventBusService.createRecord(name, address, serviceClass.getName());
        return publish(record);
    }

    private Single<Void> publish(Record record) {
        return discovery.rxPublish(record)
                .doOnSuccess(registeredRecords::add)
                .map(record1 -> (Void) null);
    }

}
