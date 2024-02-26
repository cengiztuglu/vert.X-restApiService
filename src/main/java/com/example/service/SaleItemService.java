package com.example.service;

import com.example.model.SaleItemProduct;
import com.google.gson.Gson;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import com.example.Response;


import java.util.ArrayList;
import java.util.List;

public class SaleItemService extends BaseService {

public SaleItemService(Pool databasePool, EventBus eventBus) {
        super(databasePool, eventBus);
    }

    @Override
    public void getAllItems(RoutingContext routingContext) {
        executeQueryAndRespond(routingContext, "SELECT * FROM sale_item");
    }

    @Override
    public void addItem(RoutingContext routingContext) {
        JsonArray requestArray = routingContext.getBodyAsJsonArray();

        List<JsonObject> successResponses = new ArrayList<>();

        for (Object item : requestArray) {
            if (item instanceof JsonObject) {
                JsonObject requestBody = (JsonObject) item;
                handleInsertRequest(routingContext, requestBody, requestArray, successResponses, "sale_item",eventBus ,"itemName", "vat", "price");
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
    public void updateItemById(RoutingContext routingContext) {
        JsonArray requestArray = routingContext.getBodyAsJsonArray();
        String id = routingContext.request().getParam("itemId");

        if (id == null || id.isEmpty()) {
            Response errorResponse = createErrorResponse("ID not provided", "Please provide the ID in the request");
            JsonObject errorJson = createErrorJson(errorResponse);

            routingContext.response().setStatusCode(errorResponse.getResponseCode())
                    .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                    .end(errorJson.encode());
            return;
        }

        JsonObject requestBody = requestArray.getJsonObject(0); // Sadece ilk öğeyi al
        String tableName = "sale_item";
        String idColumn = "itemId";
        String[] columnsToUpdate = {"itemName", "price", "vat"};

        // Güncellenecek değerleri al
        String itemName = requestBody.getString("itemName");
        double price = requestBody.getDouble("price");
        double vat = requestBody.getDouble("vat");


        handleUpdateRequest(routingContext, tableName, idColumn, id, itemName, price, vat);
    }




    @Override
    protected Object[] getValuesFromRequestBody(JsonObject requestBody, String[] columns) {
        Gson gson = new Gson();
        SaleItemProduct saleItemProduct = gson.fromJson(requestBody.encode(), SaleItemProduct.class);
        Object[] values = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i];
            switch (columnName) {
                case "itemName":
                    values[i] = saleItemProduct.getItemName();
                    break;
                case "vat":
                    values[i] = saleItemProduct.getVat();
                    break;
                case "price":
                    values[i] = saleItemProduct.getPrice();
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
        SaleItemProduct saleItemProduct = gson.fromJson(requestBody.encode(), SaleItemProduct.class);

        // Oluşturulan nesneyi JsonObject'e dönüştürün
      return new   JsonObject()
                .put("itemName", saleItemProduct.getItemName())
                .put("vat", saleItemProduct.getVat())
                .put("price", saleItemProduct.getPrice());


    }
}
