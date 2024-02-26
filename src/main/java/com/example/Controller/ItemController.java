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

public class ItemController extends AbstractVerticle {

    private final PayItemService payItemService;
    private final SaleItemService saleItemService;

    private final EventBus eventBus;
    private final Vertx vertx;

    public ItemController(Pool databasePool, Vertx vertx) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
        this.payItemService = new PayItemService(databasePool, eventBus);
        this.saleItemService=new  SaleItemService(databasePool,eventBus);

    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Define routes
        router.route(HttpMethod.GET, "/api/payItem").handler(this::getAllPayItem);
        router.route(HttpMethod.POST, "/api/payItem").handler(BodyHandler.create()).handler(this::addPayItem);
        router.route(HttpMethod.GET, "/api/saleItem").handler(this::getAllSaleItem);
        router.route(HttpMethod.POST, "/api/saleItem").handler(BodyHandler.create()).handler(this::addSaleItem);
        router.route(HttpMethod.PUT, "/api/saleItem/:itemId").handler(BodyHandler.create()).handler(this::updateSaleItem); // Yeni güncelleme yönlendirmesi eklendi
        router.route(HttpMethod.DELETE, "/api/saleItem/:itemId").handler(BodyHandler.create()).handler(this::updateDeleteItem); // Yeni güncelleme yönlendirmesi eklendi

        // Start the server
        server.requestHandler(router).listen(8080);
    }

    private void getAllPayItem(RoutingContext routingContext) {
        payItemService.getAllItems(routingContext);
    }

    private void addPayItem(RoutingContext routingContext) {
        payItemService.addItem(routingContext);
    }
    private void getAllSaleItem(RoutingContext routingContext) {
        saleItemService.getAllItems(routingContext);
    }

    private void addSaleItem(RoutingContext routingContext) {
        saleItemService.addItem(routingContext);
    }
    private void updateSaleItem(RoutingContext routingContext) {
        saleItemService.updateItemById(routingContext);
    }

   private  void  updateDeleteItem(RoutingContext routingContext)  {saleItemService.deleteItemById(routingContext);}


}
