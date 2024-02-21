package com.example.service;

import com.example.Response;
import com.example.model.Product;
import com.google.gson.Gson;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;

public class SaleItemService {
    private static final String CONTENT_TYPE_HEADER = "content-type";
    private static final String APPLICATION_JSON = "application/json";
    private final Pool databasePool;

    public SaleItemService(Pool databasePool) {
        this.databasePool = databasePool;
    }

    public void getAllSaleItems(RoutingContext routingContext) {
        executeQueryAndRespond(routingContext, "SELECT * FROM sale_item");
    }

    public void addSaleItem(RoutingContext routingContext, EventBus eventBus) {
        JsonArray requestArray = routingContext.getBodyAsJsonArray();

        List<JsonObject> successResponses = new ArrayList<>();

        for (Object item : requestArray) {
            if (item instanceof JsonObject) {
                JsonObject requestBody = (JsonObject) item;
                handleInsertRequest(routingContext, requestBody, requestArray, successResponses,  "sale_item",eventBus, "itemName", "vat", "price");
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

    private void executeQueryAndRespond(RoutingContext routingContext, String sqlQuery) {
        databasePool.query(sqlQuery)
                .execute()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        List<JsonObject> results = new ArrayList<>();
                        for (Row row : ar.result()) {
                            JsonObject jsonRow = new JsonObject(row.toJson().encode());
                            results.add(jsonRow);
                        }

                        routingContext.response()
                                .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                                .end(new JsonArray(results).encode());
                    } else {
                        // Handle error and create response
                        Response errorResponse = createErrorResponse("Error occurred while querying the database", ar.cause().getMessage());
                        JsonObject errorJson = createErrorJson(errorResponse);

                        routingContext.fail(errorResponse.getResponseCode(), new RuntimeException(errorJson.encode()));
                    }
                });
    }

    private void handleInsertRequest(
            RoutingContext routingContext,
            JsonObject requestBody,
            JsonArray requestArray,
            List<JsonObject> successResponses,
            String tableName,
            EventBus eventBus,
            String... columns) {

        String columnNames = String.join(", ", columns);
        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.length, "?"));

        String insertQuery = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";

        Object[] values = getProductValuesFromRequestBody(requestBody, columns);
        JsonObject eventData = new JsonObject()
                .put("itemName", requestBody.getString("itemName"))
                .put("vat", requestBody.getDouble("vat"))
                .put("price", requestBody.getDouble("price"));

// Olayı EventBus üzerinden yayınla
        eventBus.publish("sale.item.added", eventData);
        databasePool.preparedQuery(insertQuery)
                .execute(Tuple.wrap(values))
                .onComplete(ar -> {
                    JsonObject responseJson;
                    if (ar.succeeded()) {
                        long recordNumber = ar.result().property(MySQLClient.LAST_INSERTED_ID);
                        Response successResponse = createSuccessResponse("Record added successfully", recordNumber);
                        successResponses.add(createSuccessJson(successResponse));

                        if (successResponses.size() == requestArray.size()) {
                            routingContext.response()
                                    .setStatusCode(201)
                                    .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                                    .end(new JsonArray(successResponses).encode());
                        }
                    } else {
                        Response errorResponse = createErrorResponse("Error occurred while inserting into the database", ar.cause().getMessage());
                        responseJson = createErrorJson(errorResponse);
                        routingContext.response()
                                .setStatusCode(errorResponse.getResponseCode())
                                .putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                                .end(responseJson.encode());
                    }
                });
    }

    private Object[] getProductValuesFromRequestBody(JsonObject requestBody, String[] columns) {
        Gson gson = new Gson();
        Product product = gson.fromJson(requestBody.encode(), Product.class);
        Object[] values = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i];
            switch (columnName) {
                case "itemName":
                    values[i] = product.getItemName();
                    break;
                case "vat":
                    values[i] = product.getVat();
                    break;
                case "price":
                    values[i] = product.getPrice();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown column: " + columnName);
            }
        }
        return values;
    }

    private Response createSuccessResponse(String description, long recordNumber) {
        return new Response(0, description, "Record number: " + recordNumber);
    }

    private Response createErrorResponse(String description, String detail) {
        return new Response(1, description, detail);
    }

    private JsonObject createSuccessJson(Response successResponse) {
        return new JsonObject()
                .put("success", successResponse.getResponseCode())
                .put("responseDescription", successResponse.getResponseDescription())
                .put("responseDetail", successResponse.getResponseDetail());
    }

    private JsonObject createErrorJson(Response errorResponse) {
        return new JsonObject()
                .put("error", errorResponse.getResponseCode())
                .put("responseDescription", errorResponse.getResponseDescription())
                .put("responseDetail", errorResponse.getResponseDetail());
    }
}
