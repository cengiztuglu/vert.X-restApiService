package com.example.verticles;


import com.example.Controller.ItemController;
import com.example.MySQLManager;
import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        MySQLManager mySQLManager = MySQLManager.getInstance(vertx);
        vertx.deployVerticle(new ItemController(mySQLManager.getDatabasePool(), vertx));


        vertx.deployVerticle(new SaleItemAddedVertical());
        vertx.deployVerticle(new PayItemVertical());
    }
}
