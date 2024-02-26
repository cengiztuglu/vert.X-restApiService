package com.example.service;

import com.example.model.PayItemProduct;
import com.google.gson.Gson;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import com.example.Response;


import java.util.ArrayList;
import java.util.List;

public class PayItemService extends BaseService {

    public PayItemService(Pool databasePool, EventBus eventBus) {
        super(databasePool, eventBus);
    }

    @Override
    public void getAllItems(RoutingContext routingContext) {
        executeQueryAndRespond(routingContext, "SELECT * FROM pay_item");
    }

    @Override
    public void addItem(RoutingContext routingContext) {
        JsonArray requestArray = routingContext.getBodyAsJsonArray();

        List<JsonObject> successResponses = new ArrayList<>();

        for (Object item : requestArray) {
            if (item instanceof JsonObject) {
                JsonObject requestBody = (JsonObject) item;
                handleInsertRequest(routingContext, requestBody, requestArray, successResponses, "pay_item",eventBus ,"type",  "amount");
            } else {
                // Handle error: invalid request format
                Response errorResponse = createErrorResponse("Invalid request format", "Request body should be a JSON array of objects.");
                JsonObject errorJson = createErrorJson(errorResponse);

                routingContext.response()
                        .setStatusCode(errorResponse.getResponseCode())
                        .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                        .end(errorJson.encode());
                return;
            }
        }
    }

    @Override
    public void updateItemById(RoutingContext routingContext) {
        return ;
    }

    @Override
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

    @Override
    protected JsonObject createEventData(JsonObject requestBody, String[] columns) {
        Gson gson = new Gson();
        PayItemProduct payItemProduct = gson.fromJson(requestBody.encode(), PayItemProduct.class);

        // Oluşturulan nesneyi JsonObject'e dönüştürün
        return new   JsonObject()
                .put("type", payItemProduct.getType())
                .put("amount", payItemProduct.getAmount());


    }
}
