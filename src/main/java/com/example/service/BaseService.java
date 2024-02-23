package com.example.service;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Pool;
import com.example.Response;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseService {

    protected static final String CONTENT_TYPE_HEADER = "content-type";
    protected static final String APPLICATION_JSON = "application/json";
    protected final Pool databasePool;
    protected final EventBus eventBus;

    protected BaseService(Pool databasePool, EventBus eventBus) {
        this.databasePool = databasePool;
        this.eventBus = eventBus;
    }

    public abstract void getAllItems(RoutingContext routingContext);

    public abstract void addItem(RoutingContext routingContext);

    protected abstract Object[] getValuesFromRequestBody(JsonObject requestBody, String[] columns);

    protected void executeQueryAndRespond(RoutingContext routingContext, String sqlQuery) {
        databasePool.query(sqlQuery).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                List<JsonObject> results = new ArrayList<>();
                for (Row row : ar.result()) {
                    JsonObject jsonRow = new JsonObject(row.toJson().encode());
                    results.add(jsonRow);
                }

                routingContext.response().putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON).end(new JsonArray(results).encode());
            } else {
                // Handle error and create response
                Response errorResponse = createErrorResponse("Error occurred while querying the database", ar.cause().getMessage());
                JsonObject errorJson = createErrorJson(errorResponse);

                routingContext.fail(errorResponse.getResponseCode(), new RuntimeException(errorJson.encode()));
            }
        });
    }

    protected void handleInsertRequest(RoutingContext routingContext, JsonObject requestBody, JsonArray requestArray, List<JsonObject> successResponses, String tableName, EventBus eventBus, String... columns) {

        String columnNames = String.join(", ", columns);
        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.length, "?"));

        String insertQuery = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";

        Object[] values = getValuesFromRequestBody(requestBody, columns);
        JsonObject eventData = createEventData(requestBody, columns);

        // Olayı EventBus üzerinden yayınla
        eventBus.publish(tableName + ".added", eventData);
        databasePool.preparedQuery(insertQuery).execute(Tuple.wrap(values)).onComplete(ar -> {
            JsonObject responseJson;
            if (ar.succeeded()) {
                long recordNumber = ar.result().property(MySQLClient.LAST_INSERTED_ID);
                Response successResponse = createSuccessResponse(recordNumber);
                successResponses.add(createSuccessJson(successResponse));

                if (successResponses.size() == requestArray.size()) {
                    routingContext.response().setStatusCode(201).putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON).end(new JsonArray(successResponses).encode());
                }
            } else {
                Response errorResponse = createErrorResponse("Error occurred while inserting into the database", ar.cause().getMessage());
                responseJson = createErrorJson(errorResponse);
                routingContext.response().setStatusCode(errorResponse.getResponseCode()).putHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON).end(responseJson.encode());
            }
        });
    }

    protected abstract JsonObject createEventData(JsonObject requestBody, String[] columns);

    protected Response createSuccessResponse(long recordNumber) {
        return new Response(0, "Record added successfully", "Record number: " + recordNumber);
    }

    protected Response createErrorResponse(String description, String detail) {
        return new Response(1, description, detail);
    }

    protected JsonObject createSuccessJson(Response successResponse) {
        return new JsonObject().put("success", successResponse.getResponseCode()).put("responseDescription", successResponse.getResponseDescription()).put("responseDetail", successResponse.getResponseDetail());
    }

    protected JsonObject createErrorJson(Response errorResponse) {
        return new JsonObject().put("error", errorResponse.getResponseCode()).put("responseDescription", errorResponse.getResponseDescription()).put("responseDetail", errorResponse.getResponseDetail());
    }
}
