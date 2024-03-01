package com.example;

import com.example.model.PayItemProduct;
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
    public static Vertx vertx;

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


    public void addPayItemProductToDatabase(PayItemProduct payItemProduct, Handler<AsyncResult<Long>> resultHandler) {
        databasePool.getConnection(conn -> {
            if (conn.succeeded()) {
                SqlConnection connection = conn.result();

                String sql = Constant.SQLINSERT;
                Tuple params = Tuple.of(payItemProduct.getType(), payItemProduct.getAmount());

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


    public void getPayItemProductsFromDatabase(Handler<AsyncResult<List<PayItemProduct>>> resultHandler) {
        databasePool.getConnection(conn -> {
            if (conn.failed()) {
                resultHandler.handle(Future.failedFuture(conn.cause()));
                return;
            }

            SqlConnection connection = conn.result();
            String sql = Constant.SQLSELECT;

            connection.query(sql).execute(queryResult -> {
                if (queryResult.succeeded()) {
                    List<PayItemProduct> payItemProducts = new ArrayList<>();
                    RowSet<Row> rows = queryResult.result();

                    if (rows.iterator().hasNext()) {
                        rows.forEach(row -> {
                            payItemProducts.add(PayItemProduct.fromJson(row.toJson()));
                        });
                        resultHandler.handle(Future.succeededFuture(payItemProducts));
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


    public void updatePayItemProduct(PayItemProduct payItemProduct, Handler<AsyncResult<Long>> resultHandler) {
        databasePool.getConnection(conn -> {
            if (conn.succeeded()) {
                SqlConnection connection = conn.result();

                String sql = Constant.SQLUPDATE;
                Tuple params = Tuple.of(payItemProduct.getType(), payItemProduct.getAmount(), payItemProduct.getPayId());

                PreparedQuery<RowSet<Row>> preparedQuery = connection.preparedQuery(sql);
                preparedQuery.execute(params, res -> {


                    if (res.succeeded()) {
                        Long updatedPayId = (long) payItemProduct.getPayId();

                        resultHandler.handle(Future.succeededFuture(updatedPayId));

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

public  void  deletePayItem(Integer payItemId,Handler<AsyncResult<Long>>resultHandler)
{
    databasePool.getConnection(conn->{
        if (conn.succeeded())
        {
            SqlConnection connection=conn.result();
            String sql=Constant.SQLDELETE;
            Tuple params=Tuple.of(payItemId);
            PreparedQuery<RowSet<Row>>preparedQuery=connection.preparedQuery(sql);
            preparedQuery.execute(params,res->{
                if(res.succeeded())
                {
                    Long deletePayId=(long) payItemId;
                    resultHandler.handle(Future.succeededFuture(deletePayId));
                }
                else{
                    resultHandler.handle(Future.failedFuture(res.cause()));
                }
                connection.close();

            });

        }
    });
}
}


