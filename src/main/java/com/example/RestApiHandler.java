package com.example;

import com.example.service.SaleItemService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;
import verticles.SaleItemAddedVerticle;

public class RestApiHandler extends AbstractVerticle {

    private final SaleItemService saleItemService;
    private  EventBus eventBus;


    public RestApiHandler(Pool databasePool) {
        this.saleItemService = new SaleItemService(databasePool);

    }



    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        eventBus = vertx.eventBus();


        // Define routes
        router.route(HttpMethod.GET, "/api/saleItem").handler(this::getAllSaleItem);
        router.route(HttpMethod.POST, "/api/saleItem").handler(BodyHandler.create()).handler(this::addSaleItem);

        // Start the server
        server.requestHandler(router).listen(8080);

    }


    private void getAllSaleItem(RoutingContext routingContext) {
        saleItemService.getAllSaleItems(routingContext);
    }

    private void addSaleItem(RoutingContext routingContext) {
        saleItemService.addSaleItem(routingContext,vertx.eventBus());


    }
}
