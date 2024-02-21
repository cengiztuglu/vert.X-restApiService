package com.example;


import io.vertx.core.Vertx;
import verticles.SaleItemAddedVerticle;

public class MainVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        MySQLManager mySQLManager = MySQLManager.getInstance(vertx);
        vertx.deployVerticle(new RestApiHandler(mySQLManager.getDatabasePool()));

        vertx.deployVerticle(new SaleItemAddedVerticle());
    }
}
