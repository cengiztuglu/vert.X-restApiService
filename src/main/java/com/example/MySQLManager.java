package com.example;

import com.example.model.PayItemProduct;
import com.google.gson.Gson;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;




public class MySQLManager {


    public static final String DATABASE = "database";


    private static volatile MySQLManager thisInstance;
    public static Pool databasePool;
    public static Vertx vertx;
    public MySQLManager(Vertx vertx) {
        this.vertx = vertx;
    }
    public static MySQLManager getInstance() {
        if (thisInstance == null) {
            throw new IllegalStateException("Can not call before init");
        } else {
            return thisInstance;
        }
    }
    public static void init(Vertx vertx) {
        thisInstance = new MySQLManager(vertx);


        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(
                new ConfigStoreOptions()
                        .setType("file")
                        .setConfig(new JsonObject().put("path", "src/main/resources/app.json")));

        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        CompletableFuture<JsonObject> configFuture = new CompletableFuture<>();

        retriever.getConfig().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject config = ar.result();

                MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                        .setPort(config.getJsonObject(DATABASE).getInteger("port"))
                        .setHost(config.getJsonObject(DATABASE).getString("host"))
                        .setDatabase(config.getJsonObject(DATABASE).getString("name"))
                        .setUser(config.getJsonObject(DATABASE).getString("user"))
                        .setPassword(config.getJsonObject(DATABASE).getString("password"))
                        .addProperty("characterEncoding", "UTF-8");

                PoolOptions poolOptions = new PoolOptions()
                        .setMaxSize(5);

                databasePool = MySQLPool.pool(vertx, connectOptions, poolOptions);
                configFuture.complete(ar.result());
            } else {
                configFuture.completeExceptionally(ar.cause());
            }
        });
    }




    public void handleInsertRequest( JsonObject requestBody,  List<JsonObject> successResponses, String tableName,  String... columns) {

        String columnNames = String.join(", ", columns);
        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.length, "?"));

        String insertQuery = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";

        Object[] values = getValuesFromRequestBody(requestBody, columns);
        System.out.println("Values: " + Arrays.toString(values));  // Loglama ekledik

        MySQLManager.databasePool.preparedQuery(insertQuery).execute(Tuple.wrap(values)).onComplete(ar -> {
            if (ar.succeeded()) {
                long recordNumber = ar.result().property(MySQLClient.LAST_INSERTED_ID);
                Response successResponse = createSuccessResponse(recordNumber);
                successResponses.add(createSuccessJson(successResponse));


            } else {
                System.out.println("Insert Error: " + ar.cause().getMessage());
            }
        });
    }




    public Response createSuccessResponse(long recordNumber) {
        return new Response(0, "Record added successfully", "Record number: " + recordNumber);
    }

    public Response createErrorResponse(String description, String detail) {
        return new Response(1, description, detail);
    }

    public JsonObject createSuccessJson(Response successResponse) {
        return new JsonObject().put("success", successResponse.getResponseCode()).put("responseDescription", successResponse.getResponseDescription()).put("responseDetail", successResponse.getResponseDetail());
    }

    public JsonObject createErrorJson(Response errorResponse) {
        return new JsonObject().put("error", errorResponse.getResponseCode()).put("responseDescription", errorResponse.getResponseDescription()).put("responseDetail", errorResponse.getResponseDetail());
    }
   public void executeQueryAndRespond( String sqlQuery) {
        MySQLManager.databasePool.query(sqlQuery).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                List<JsonObject> results = new ArrayList<>();
                for (Row row : ar.result()) {
                    JsonObject jsonRow = new JsonObject(row.toJson().encode());
                    results.add(jsonRow);
                }

            }
        });
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


}
