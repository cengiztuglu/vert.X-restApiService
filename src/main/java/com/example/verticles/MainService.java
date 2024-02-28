package com.example.verticles;

import com.example.MySQLManager;
import com.example.service.PayItemServiceVerticle;
import com.example.service.SaleItemServiceVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.example.MySQLManager.databasePool;


public class MainService {

    private final Router router;
    private final PayItemServiceVerticle payItemService;
    private final SaleItemServiceVerticle saleItemService;
    public static Vertx vertx;
    private static final Logger logger = LoggerFactory.getLogger(MainService.class);

    public MainService(Vertx vertx) {
        MainService.vertx = vertx;

        this.router = Router.router(MainService.vertx);
        this.payItemService = new PayItemServiceVerticle(databasePool, vertx.eventBus());
        this.saleItemService = new SaleItemServiceVerticle(databasePool, vertx.eventBus());
        startService();
    }

    public static void main(String[] args) {
        MainService.vertx = Vertx.vertx();
        MySQLManager.init(MainService.vertx);


    vertx.deployVerticle(SaleItemAddedVerticle.class.getName(), new DeploymentOptions());
    vertx.deployVerticle(PayItemVerticle.class.getName(), new DeploymentOptions());
    MainService mainService = new MainService(vertx);
    mainService.startService();


    logger.error("MySQLManager initialization failed. databasePool is null.");


    }

    private void startService() {
        ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(
                new ConfigStoreOptions()
                        .setType("file")
                        .setConfig(new JsonObject().put("path", "src/main/resources/app.json")));

        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        retriever.getConfig().onComplete(ar -> {
            if (ar.succeeded()) {
                ConfigStoreOptions httpStore = new ConfigStoreOptions()
                        .setType("http")
                        .setConfig(new JsonObject()
                                .put("host", "localhost")
                                .put("port", 8080)
                                .put("path", "/conf")
                                .put("headers", new JsonObject().put("Accept", "app.json")));

                options.addStore(httpStore).addStore(sysPropsStore);

                JsonObject config = ar.result();

                logger.info("Received Configuration: {}", config.encodePrettily());

                String httpHost = config.getJsonObject("Server").getString("host");
                int port = config.getJsonObject("Server").getInteger("port");
                logger.info("HTTP Host: {}", httpHost);

                HttpServerOptions httpServerOptions = new HttpServerOptions();
                httpServerOptions.setHost(httpHost);
                httpServerOptions.setPort(port);

                HttpServer server = vertx.createHttpServer(httpServerOptions);
                server.requestHandler(router).listen(port);
                addRoutes(router);

            } else {
                logger.error("An error occurred:", ar.cause());
            }
        });

        retriever.listen(configChange -> {
            JsonObject newConfig=configChange.getNewConfiguration();
            logger.info("Configuration has been updated: {}", newConfig.encodePrettily());

        });
    }





    private void addRoutes(Router router) {
        router.route().method(io.vertx.core.http.HttpMethod.GET).path("/api/payItem").handler(this::getAllPayItem);
        router.route().method(io.vertx.core.http.HttpMethod.POST).path("/api/payItem").handler(BodyHandler.create()).handler(this::addPayItem);
        router.route().method(io.vertx.core.http.HttpMethod.GET).path("/api/saleItem").handler(this::getAllSaleItem);
        router.route().method(io.vertx.core.http.HttpMethod.POST).path("/api/saleItem").handler(BodyHandler.create()).handler(this::addSaleItem);
        router.route().method(io.vertx.core.http.HttpMethod.PUT).path("/api/saleItem/:itemId").handler(BodyHandler.create()).handler(this::updateSaleItem);
        router.route().method(io.vertx.core.http.HttpMethod.DELETE).path("/api/saleItem/:itemId").handler(BodyHandler.create()).handler(this::updateDeleteItem);
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

    private void updateDeleteItem(RoutingContext routingContext) {
        saleItemService.deleteItemById(routingContext);
    }
}