package com.example.verticle;

import com.example.MySQLManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;


public class MainVerticle extends AbstractVerticle {
    public static void main(String[] args) {
        io.vertx.core.Vertx.vertx().deployVerticle(new MainVerticle());
    }
    @Override
    public void start() {
        Vertx vertx = Vertx.vertx();

        MySQLManager.init(vertx);
        vertx.deployVerticle(PayItemVerticle.class.getName());
    }
}
