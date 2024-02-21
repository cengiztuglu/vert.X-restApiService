package com.example;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

public class MySQLManager {

    private static volatile MySQLManager instance;
    private final Pool databasePool;

    private MySQLManager(Vertx vertx) {
        // Your existing constructor logic here
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("deneme")
                .setUser("root")
                .setPassword("password")
                .addProperty("characterEncoding","UTF-8");

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        databasePool = MySQLPool.pool(vertx, connectOptions, poolOptions);

    }

    // Double-checked locking for thread safety
    public static MySQLManager getInstance(Vertx vertx) {
        if (instance == null) {
            synchronized (MySQLManager.class) {
                if (instance == null) {
                    instance = new MySQLManager(vertx);
                }
            }
        }
        return instance;
    }

    public Pool getDatabasePool() {
        return databasePool;
    }


}
