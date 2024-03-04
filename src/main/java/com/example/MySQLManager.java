package com.example;

import com.example.constants.PayItemConst;
import com.example.constants.SaleItemConst;
import com.example.model.PayItemProduct;
import com.example.model.SaleItemProduct;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MySQLManager {

    public static final String DATABASE = "database";
    private static volatile MySQLManager thisInstance;
    private static Pool databasePool;
    private static Vertx vertx;

    public MySQLManager(Vertx vertx) {
        MySQLManager.vertx = vertx;
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


    public <T> void addProductToDatabase(T product, Handler<AsyncResult<Long>> resultHandler) {
        databasePool.getConnection(conn -> {
            if (conn.succeeded()) {
                SqlConnection connection = conn.result();

                String sql;
                Tuple params;

                if (product instanceof PayItemProduct) {
                    PayItemProduct payItemProduct = (PayItemProduct) product;
                    sql = PayItemConst.SQLINSERT;
                    params = Tuple.of(payItemProduct.getType(), payItemProduct.getAmount());
                } else if (product instanceof SaleItemProduct) {
                    SaleItemProduct saleItemProduct = (SaleItemProduct) product;
                    sql = SaleItemConst.SQLINSERT;
                    params = Tuple.of(saleItemProduct.getItemName(), saleItemProduct.getPrice(), saleItemProduct.getVat());
                } else {
                    resultHandler.handle(Future.failedFuture("Unsupported product type"));
                    connection.close();
                    return;
                }

                PreparedQuery<RowSet<Row>> preparedQuery = connection.preparedQuery(sql);
                preparedQuery.execute(params, res -> {
                    if (res.succeeded()) {
                        resultHandler.handle(Future.succeededFuture(res.result().property(MySQLClient.LAST_INSERTED_ID)));
                    } else {

                        resultHandler.handle(Future.failedFuture(res.cause()));
                    }
                    connection.close();
                });
            } else {
                resultHandler.handle(Future.failedFuture(conn.cause()));
            }
        });
    }


    private <T> void getProductsFromDatabase(String tableName, Class<T> productClass, Handler<AsyncResult<List<T>>> resultHandler) {
        databasePool.getConnection(conn -> {
            if (conn.failed()) {
                resultHandler.handle(Future.failedFuture(conn.cause()));
                return;
            }

            SqlConnection connection = conn.result();
            String sql = "SELECT * FROM " + tableName;

            connection.query(sql).execute(queryResult -> {
                if (queryResult.succeeded()) {
                    List<T> products = new ArrayList<>();
                    RowSet<Row> rows = queryResult.result();

                    if (rows.iterator().hasNext()) {
                        rows.forEach(row -> {
                            JsonObject json = row.toJson();

                            T product = json.mapTo(productClass);

                            products.add(product);
                        });
                        resultHandler.handle(Future.succeededFuture(products));
                    } else {
                        resultHandler.handle(Future.failedFuture("RowSet is empty"));
                    }
                } else {
                    resultHandler.handle(Future.failedFuture(queryResult.cause()));
                }

                connection.close();
            });
        });
    }

    public void getSaleItemProductsFromDatabase(Handler<AsyncResult<List<SaleItemProduct>>> resultHandler) {
        getProductsFromDatabase(SaleItemConst.TABLE_NAME, SaleItemProduct.class, resultHandler);
    }

    public void getPayItemProductsFromDatabase(Handler<AsyncResult<List<PayItemProduct>>> resultHandler) {
        getProductsFromDatabase(PayItemConst.TABLE_NAME, PayItemProduct.class, resultHandler);
    }




    public <T> void updateProductToDatabase(T product, Handler<AsyncResult<Long>> resultHandler) {
        databasePool.getConnection(conn -> {
            if (conn.succeeded()) {
                SqlConnection connection = conn.result();
                String sql;
                Tuple params;
                if (product instanceof PayItemProduct) {
                    PayItemProduct payItemProduct = (PayItemProduct) product;
                    sql = PayItemConst.SQLUPDATE;
                    params = Tuple.of(payItemProduct.getType(), payItemProduct.getAmount(), payItemProduct.getPayId());

                } else if (product instanceof SaleItemProduct) {
                    SaleItemProduct saleItemProduct = (SaleItemProduct) product;
                    sql = SaleItemConst.SQLUPDATE;
                    params = Tuple.of(saleItemProduct.getItemName(), saleItemProduct.getPrice(), saleItemProduct.getVat(), saleItemProduct.getItemId());

                } else {
                    resultHandler.handle(Future.failedFuture("unsupported product type"));
                    connection.close();
                    return;
                }
                PreparedQuery<RowSet<Row>> preparedQuery = connection.preparedQuery(sql);
                preparedQuery.execute(params, res -> {
                    if (res.succeeded()) {
                        if (res.result().rowCount() > 0) {
                            resultHandler.handle(Future.succeededFuture());
                        } else {
                            resultHandler.handle(Future.failedFuture("No rows affected"));
                        }

                    } else {
                        resultHandler.handle(Future.failedFuture(res.cause()));

                    }
                    connection.close();
                });

            }
        });
    }



    public <T> void deleteProductFromDatabase(Long productId, Class<T> productClass, Handler<AsyncResult<Void>> resultHandler) {
        databasePool.getConnection(conn -> {
            if (conn.succeeded()) {
                SqlConnection connection = conn.result();
                String sql;
                Tuple params;

                if (productClass.equals(PayItemProduct.class)) {
                    sql = PayItemConst.SQLDELETE;
                    params = Tuple.of(productId);
                } else if (productClass.equals(SaleItemProduct.class)) {
                    sql = SaleItemConst.SQLDELETE;
                    params = Tuple.of(productId);
                } else {
                    resultHandler.handle(Future.failedFuture("unsupported product type"));
                    connection.close();
                    return;
                }

                PreparedQuery<RowSet<Row>> preparedQuery = connection.preparedQuery(sql);
                preparedQuery.execute(params, res -> {
                    if (res.succeeded()) {
                        if (res.result().rowCount() > 0) {
                            resultHandler.handle(Future.succeededFuture());
                        } else {
                            resultHandler.handle(Future.failedFuture("No rows affected"));
                        }
                    } else {
                        resultHandler.handle(Future.failedFuture(res.cause()));
                    }

                    connection.close();
                });
            } else {
                resultHandler.handle(Future.failedFuture(conn.cause()));
            }
        });
    }

}


