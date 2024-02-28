package com.example;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;

public class MySQLManager {

    public static Vertx vertx;

    private static volatile MySQLManager thisInstance;
    public static Pool databasePool;
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
                        .setPort(config.getJsonObject("database").getInteger("port"))
                        .setHost(config.getJsonObject("database").getString("host"))
                        .setDatabase(config.getJsonObject("database").getString("name"))
                        .setUser(config.getJsonObject("database").getString("user"))
                        .setPassword(config.getJsonObject("database").getString("password"))
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
}
