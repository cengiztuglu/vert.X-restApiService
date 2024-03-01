package com.example.service;

import com.example.Constant;
import com.example.MySQLManager;
import com.example.verticle.PayItemVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


public class MainService  {
    private  final Router router;
    public static Vertx vertx;
    private static final Logger logger = LoggerFactory.getLogger(MainService.class);
    public MainService(Vertx vertx) {
        MainService.vertx = vertx;

        this.router = Router.router(MainService.vertx);

    }

    public static void main(String[] args) {
        MainService.vertx = Vertx.vertx();

        MySQLManager.init(vertx);
        MainService mainService = new MainService(vertx);
        mainService.startService();

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
                                .put("port", 8081)
                                .put("path", "/conf")
                                .put("headers", new JsonObject().put("Accept", "app.json")));

                options.addStore(httpStore).addStore(sysPropsStore);

                JsonObject config = ar.result();

                logger.info("Received Configuration: {}");

                String httpHost = config.getJsonObject("Server").getString("host");
                int port = config.getJsonObject("Server").getInteger("port");
                System.out.println( httpHost);
                System.out.println( port);
                HttpServerOptions httpServerOptions = new HttpServerOptions();
                httpServerOptions.setHost(httpHost);
                httpServerOptions.setPort(port);

                HttpServer server = vertx.createHttpServer(httpServerOptions);
                server.requestHandler(this.router).listen(port);
                addRoutes(router);
                vertx.deployVerticle(PayItemVerticle.class.getName());


            } else {
                logger.error("An error occurred:", ar.cause());
            }
        });
        retriever.listen(configChange -> {
            JsonObject newConfig = configChange.getNewConfiguration();
            logger.info("Configuration has been updated: {}");

        });


    }

    private void addRoutes(Router router) {
        this.router.post("/payitem").handler(this::handleHttpPostPayItem);

    }

    private void handleHttpPostPayItem(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        request.bodyHandler(buffer -> {
            JsonObject payItemJson = new JsonObject(buffer.toString());

            vertx.eventBus().request(Constant.ITEMADD, payItemJson.encode(), reply -> {
                if (reply.succeeded()) {
                    Object body = reply.result().body();

                    JsonObject responseJson;
                    if (body instanceof String) {
                        try {
                            responseJson = new JsonObject((String) body);
                        } catch (Exception e) {
                            responseJson = new JsonObject()
                                    .put("responseCode", 500)
                                    .put("responseDescription", "Internal Server Error")
                                    .put("responseDetail", "Error parsing JSON response");
                            routingContext.response()
                                    .putHeader("content-type", "application/json")
                                    .setStatusCode(500)
                                    .end(responseJson.encode());
                            return;
                        }
                    } else if (body instanceof JsonObject) {
                        responseJson = (JsonObject) body;
                    } else {
                        responseJson = new JsonObject()
                                .put("responseCode", 500)
                                .put("responseDescription", "Internal Server Error")
                                .put("responseDetail", "Unknown response format");
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(500)
                                .end(responseJson.encode());
                        return;
                    }

                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(responseJson.encode());
                }
            });
        });
    }



}
