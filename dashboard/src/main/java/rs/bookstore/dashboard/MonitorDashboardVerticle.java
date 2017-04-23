/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.bookstore.dashboard;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.rest.ServiceDiscoveryRestEndpoint;
import rs.bookstore.lib.MicroServiceVerticle;

/**
 *
 * @author marko
 */
public class MonitorDashboardVerticle extends MicroServiceVerticle {

    @Override
    public void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.

        Router router = Router.router(vertx);

        // create Dropwizard metrics service
        MetricsService metricsService = MetricsService.create(vertx);

        //event bus bridge
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions bridgeOptions = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("microservice.metrics"));
        sockJSHandler.bridge(bridgeOptions);

        router.route("/eventbus/*").handler(sockJSHandler);

        
        //discovery endpoint
        ServiceDiscoveryRestEndpoint.create(router, discovery);

        //static content
        router.route("/*").handler(StaticHandler.create());

        int port = config().getInteger("dashboard.server.port", 9999);
        String host = config().getString("dashboard.server.host", "localhost");

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host);

        //periodically send metrics to the event bus
        vertx.setPeriodic(config().getInteger("metrics.interval", 3000), time -> {
            JsonObject metrics = metricsService.getMetricsSnapshot(vertx);
            
            System.out.println("Metrics: "+metrics);
            vertx.eventBus().publish("microservice.metrics", metrics);
        });
    }

}
