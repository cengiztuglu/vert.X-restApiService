package com.example.verticles;

import com.example.MySQLManager;
import com.example.model.PayItemProduct;
import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.example.Response;


import java.util.ArrayList;
import java.util.List;

public class PayItemVerticle extends AbstractVerticle {

    public static final String CONTENT_TYPE_HEADER = "content-type";
    public static final String APPLICATION_JSON = "application/json";


    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("payItem.add", this::handleAddItemMessage);





    }

    private void handleAddItemMessage(Message<JsonArray> message) {
        JsonArray requestArray = message.body();
        addItem(requestArray, result -> {
            if (result.succeeded()) {
                message.reply("Ürünler başarıyla eklendi");
            } else {
                message.fail(500, "Ürünler eklenirken bir hata oluştu");
            }
        });
    }

    public void getAllItems(RoutingContext routingContext) {
        MySQLManager.getInstance().executeQueryAndRespond("SELECT * FROM pay_item");
    }



    public void addItem(JsonArray requestArray, Handler<AsyncResult<Void>> resultHandler) {
        List<JsonObject> successResponses = new ArrayList<>();

        // Asenkron işlem başlangıcı
        vertx.executeBlocking(future -> {
            for (Object item : requestArray) {
                if (item instanceof JsonObject) {
                    JsonObject requestBody = (JsonObject) item;
                    MySQLManager.getInstance().handleInsertRequest(requestBody, successResponses, "pay_item", "type", "amount");
                } else {
                    Response errorResponse = MySQLManager.getInstance().createErrorResponse("Invalid request format", "Request body should be a JSON array of objects.");
                    JsonObject errorJson = MySQLManager.getInstance().createErrorJson(errorResponse);

                    // Eğer hata durumu varsa future ile bildiriyoruz.
                    future.fail("Invalid request format");
                    return;
                }
            }

            future.complete();
        }, resultHandler);
    }



        protected Object[] getValuesFromRequestBody(JsonObject requestBody, String[] columns) {
        Gson gson = new Gson();
        PayItemProduct payItemProduct = gson.fromJson(requestBody.encode(), PayItemProduct.class);
        Object[] values = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i];
            switch (columnName) {
                case "type":
                    values[i] = payItemProduct.getType();
                    break;
                case "amount":
                    values[i] = payItemProduct.getAmount();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown column: " + columnName);
            }
        }
        return values;
    }


    protected JsonObject createEventData(JsonObject requestBody, String[] columns) {
        Gson gson = new Gson();
        PayItemProduct payItemProduct = gson.fromJson(requestBody.encode(), PayItemProduct.class);

        // Oluşturulan nesneyi JsonObject'e dönüştürün
        return new JsonObject()
                .put("type", payItemProduct.getType())
                .put("amount", payItemProduct.getAmount());


    }
}
