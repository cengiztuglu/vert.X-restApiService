package com.example;


import com.example.Controller.ItemController;
import com.example.verticles.PayItemVertical;
import com.example.verticles.SaleItemAddedVertical;
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
