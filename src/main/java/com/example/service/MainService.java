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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


public class MainService {
    private final Router router;
    private static Vertx vertx;
    private static final Logger logger = LoggerFactory.getLogger(MainService.class);

    public MainService(Vertx vertx) {

        this.router = Router.router(MainService.vertx);
        vertx.deployVerticle(PayItemVerticle.class.getName());


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
                HttpServerOptions httpServerOptions = new HttpServerOptions();
                httpServerOptions.setHost(httpHost);
                httpServerOptions.setPort(port);

                HttpServer server = vertx.createHttpServer(httpServerOptions);
                server.requestHandler(this.router).listen(port);
                addRoutes(router);


            } else {
                logger.error("An error occurred:", ar.cause());
            }
        });
        retriever.listen(configChange -> {
            JsonObject newConfig = configChange.getNewConfiguration();
            logger.info(newConfig.encode());

        });
    }
    private void addRoutes(Router router) {
        router.post("/payItem").handler(this::handleHttpPostPayItem);
        router.get("/payItem").handler(this::handleHttpGetPayItems);
        router.put("/payItem/:payItemId").handler(this::handleHttpPutPayItemUpdate);
        router.delete("/payItem/:payItemId").handler(this::handleHttpDelete);


    }




    private void handleHttpGetPayItems(RoutingContext routingContext) {
        vertx.eventBus().request(Constant.ITEMLIST, "", reply -> {
            if (reply.succeeded()) {
                Object body = reply.result().body();

                if (body instanceof String) {
                    String responseBody = (String) body;
                    try {
                        JsonArray responseArray = new JsonArray(responseBody);

                        JsonObject responseJson = new JsonObject().put("items", responseArray);

                        routingContext.response()
                                .putHeader(Constant.CONTENT, Constant.APPLICATION)
                                .end(responseJson.encode());
                    } catch (Exception e) {
                        routingContext.fail(500);
                    }
                } else {
                    routingContext.fail(500);
                }
            }
        });
    }


    private void handleHttpPostPayItem(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        request.bodyHandler(buffer -> {
            JsonObject payItemJson = new JsonObject(buffer.toString());
            processEventBusRequest(routingContext, Constant.ITEMADD, payItemJson);
        });
    }

    private void handleHttpPutPayItemUpdate(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String payItemId = routingContext.request().getParam("payItemId");

        request.bodyHandler(buffer -> {
            JsonObject payItemJson = new JsonObject(buffer.toString());
            payItemJson.put("payId", Integer.parseInt(payItemId));
            processEventBusRequest(routingContext, Constant.ITEMPUT, payItemJson);
        });
    }


    private void handleHttpDelete(RoutingContext routingContext)
    {
        HttpServerRequest request=routingContext.request();
        Long payItemId= Long.valueOf(routingContext.request().getParam("payItemId"));
        request.bodyHandler(buffer->{
            JsonObject payItemJson=new JsonObject(buffer.toString());
            payItemJson.put("payId",Integer.parseInt(String.valueOf(payItemId)));
            processEventBusRequest(routingContext,Constant.ITEMDELETE,payItemJson);
        });
    }



    private void processEventBusRequest(RoutingContext routingContext, String eventBusAddress, JsonObject payload) {
        vertx.eventBus().request(eventBusAddress, payload.encode(), reply -> {
            if (reply.succeeded()) {
                handleEventBusSuccess(routingContext, reply.result().body());
            } else {
                routingContext.fail(500);
            }
        });
    }

    private void handleEventBusSuccess(RoutingContext routingContext, Object body) {
        JsonObject responseJson = createResponseJson(body);
        routingContext.response()
                .putHeader(Constant.CONTENT, Constant.APPLICATION)
                .end(responseJson.encode());
    }

    private JsonObject createResponseJson(Object body) {
        if (body instanceof String) {
            try {
                return new JsonObject((String) body);
            } catch (Exception e) {
                return createErrorResponseJson("Error parsing JSON response");
            }
        } else if (body instanceof JsonObject) {
            return (JsonObject) body;
        } else {
            return createErrorResponseJson("Unknown response format");
        }
    }

    private JsonObject createErrorResponseJson(String errorDetail) {
        return new JsonObject()
                .put(Constant.RESPONSECOD, 500)
                .put(Constant.RESPONDESC, Constant.INTERNALS)
                .put(Constant.RESPONSEDETAIL, errorDetail);
    }
}
