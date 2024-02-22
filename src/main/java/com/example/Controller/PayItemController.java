package com.example.Controller;

import com.example.service.PayItemService;
import com.example.service.SaleItemService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;

public class PayItemController extends AbstractVerticle {

    private final PayItemService payItemService;

    private final EventBus eventBus;
    private final Vertx vertx;

    public PayItemController(Pool databasePool,Vertx vertx) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
        this.payItemService = new PayItemService(databasePool, eventBus);
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Define routes
        router.route(HttpMethod.GET, "/api/payItem").handler(this::getAllPayItem);
        router.route(HttpMethod.POST, "/api/payItem").handler(BodyHandler.create()).handler(this::addPayItem);

        // Start the server
        server.requestHandler(router).listen(8080);
    }

    private void getAllPayItem(RoutingContext routingContext) {
        payItemService.getAllItems(routingContext);
    }

    private void addPayItem(RoutingContext routingContext) {
        payItemService.addItem(routingContext);
    }
}
