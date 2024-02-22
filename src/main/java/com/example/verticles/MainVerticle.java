package com.example.verticles;


import com.example.Controller.PayItemController;
import com.example.MySQLManager;
import com.example.Controller.SaleItemController;
import io.vertx.core.Vertx;

public class MainVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        MySQLManager mySQLManager = MySQLManager.getInstance(vertx);
        vertx.deployVerticle(new SaleItemController(mySQLManager.getDatabasePool(), vertx));
        vertx.deployVerticle(new PayItemController(mySQLManager.getDatabasePool(),vertx));




        vertx.deployVerticle(new SaleItemAddedVerticle());
        vertx.deployVerticle(new PayItemVerticle());
    }
}
