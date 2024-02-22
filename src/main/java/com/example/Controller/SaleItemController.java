package com.example.Controller;

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

public class SaleItemController extends AbstractVerticle {

    private final SaleItemService saleItemService;

    private final EventBus eventBus;
    private final Vertx vertx;

    public SaleItemController(Pool databasePool,Vertx vertx) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
        this.saleItemService = new SaleItemService(databasePool, eventBus);
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Define routes
        router.route(HttpMethod.GET, "/api/saleItem").handler(this::getAllSaleItem);
        router.route(HttpMethod.POST, "/api/saleItem").handler(BodyHandler.create()).handler(this::addSaleItem);

        // Start the server
        server.requestHandler(router).listen(8080);
    }

    private void getAllSaleItem(RoutingContext routingContext) {
        saleItemService.getAllItems(routingContext);
    }

    private void addSaleItem(RoutingContext routingContext) {
        saleItemService.addItem(routingContext);
    }
}
