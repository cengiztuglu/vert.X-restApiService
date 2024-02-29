package com.example.verticle;

import com.example.MySQLManager;
import com.example.Response;
import com.example.model.PayItemProduct;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class PayItemVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.post("/payitem").handler(this::handleHttpPostPayItem);

        server.requestHandler(router).listen(8080, http -> {
            if (http.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(http.cause());
            }
        });

        vertx.eventBus().consumer("payItem.add", this::handleAddPayItem);
    }

    private void handleHttpPostPayItem(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        request.bodyHandler(buffer -> {
            JsonObject payItemJson = new JsonObject(buffer.toString());

            vertx.eventBus().request("payItem.add", payItemJson.encode(), reply -> {
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
                } else {
                    Throwable cause = reply.cause();
                    JsonObject errorResponse = new JsonObject()
                            .put("responseCode", 500)
                            .put("responseDescription", "Internal Server Error")
                            .put("responseDetail", cause != null ? cause.getMessage() : "Unknown error");
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .setStatusCode(500)
                            .end(errorResponse.encode());
                }
            });
        });
    }

    private void handleAddPayItem(Message<String> message) {
        String payItemJsonString = message.body();
        JsonObject payItemJson = new JsonObject(payItemJsonString);

        MySQLManager.getInstance().addPayItemProductToDatabase(PayItemProduct.fromJson(payItemJson), result -> {
            if (result.succeeded()) {
                Response successResponse = new Response(0, "PayItemProduct ekleme başarılı", "PayItemProduct ID: " + result.result());
                message.reply(successResponse.toJson());
            } else {
                Response errorResponse = new Response(500, "PayItemProduct ekleme sırasında bir hata oluştu", result.cause().getMessage());
                message.reply(errorResponse.toJson());
            }
        });
    }
}
