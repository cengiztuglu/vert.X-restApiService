package com.example;

import com.example.model.Product;
import com.google.gson.Gson;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;

public class SqlService {

    private final Pool databasePool;

    public SqlService(Pool databasePool) {
        this.databasePool = databasePool;
    }

    public void getAllSaleItems(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sqlQuery = "SELECT * FROM saleitem";
        executeQuery(sqlQuery, resultHandler);
    }

    public void addSaleItem(JsonObject requestBody, Handler<AsyncResult<JsonObject>> resultHandler) {
        String tableName = "saleitem";
        String[] columns = {"itemName", "vat", "price"};

        String columnNames = String.join(", ", columns);
        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.length, "?"));

        String insertQuery = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";

        Object[] values = getProductValuesFromRequestBody(requestBody, columns);

        databasePool.preparedQuery(insertQuery)
                .execute(Tuple.tuple(Arrays.asList(values)))
                .onComplete(ar -> handleDatabaseResponse(ar, resultHandler));
    }

    private void executeQuery(String sqlQuery, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        databasePool.query(sqlQuery)
                .execute()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        List<JsonObject> results = new ArrayList<>();
                        for (Row row : ar.result()) {
                            JsonObject jsonRow = new JsonObject(row.toJson().encode());
                            results.add(jsonRow);
                        }
                        resultHandler.handle(io.vertx.core.Future.succeededFuture(results));
                    } else {
                        handleDatabaseError(resultHandler, ar.cause());
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

    private void handleDatabaseResponse(io.vertx.core.AsyncResult<?> ar, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonObject responseJson;
        if (ar.succeeded()) {
            // Başarı durumu
            responseJson = createSuccessJson("Record added successfully");
            resultHandler.handle(io.vertx.core.Future.succeededFuture(responseJson));
        } else {
            // Hata durumu
            Response errorResponse = createErrorResponse("Error occurred while interacting with the database", ar.cause().getMessage());
            responseJson = createErrorJson(errorResponse);
            handleDatabaseError(resultHandler, responseJson, errorResponse.getResponseCode());
        }
    }

    private void handleDatabaseError(Handler<AsyncResult<List<JsonObject>>> resultHandler, Throwable cause) {
        Response errorResponse = createErrorResponse("Error occurred while querying the database", cause.getMessage());
        JsonObject errorJson = createErrorJson(errorResponse);
        resultHandler.handle(io.vertx.core.Future.failedFuture(new RuntimeException(errorJson.encode())));
    }

    private void handleDatabaseError(Handler<AsyncResult<JsonObject>> resultHandler, JsonObject errorJson, int statusCode) {
        resultHandler.handle(io.vertx.core.Future.failedFuture(new RuntimeException(errorJson.encode())));
    }

    private JsonObject createSuccessJson(String description) {
        return new JsonObject()
                .put("success", 0)
                .put("responseDescription", description);
    }

    private Response createErrorResponse(String description, String detail) {
        return new Response(1, description, detail);
    }

    private JsonObject createErrorJson(Response errorResponse) {
        return new JsonObject()
                .put("error", errorResponse.getResponseCode())
                .put("responseDescription", errorResponse.getResponseDescription())
                .put("responseDetail", errorResponse.getResponseDetail());
    }
}
