package com.example.verticle;

import com.example.Constant;
import com.example.MySQLManager;
import com.example.Response;
import com.example.model.PayItemProduct;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import io.vertx.core.json.JsonObject;


public class PayItemVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(Constant.ITEMADD, this::handleAddPayItem);
    }


    private void handleAddPayItem(Message<String> message) {
        String payItemJsonString = message.body();
        JsonObject payItemJson = new JsonObject(payItemJsonString);

        MySQLManager.getInstance().addPayItemProductToDatabase(PayItemProduct.fromJson(payItemJson), result -> {
            if (result.succeeded()) {
                Response successResponse = new Response(0, "PayItemProduct ekleme başarılı", "PayItemProduct ID: " + result.result());
                message.reply(successResponse.toJson());
            } else {
                Response errorResponse = new Response(500, "PayItemProduct ekleme sırasında bir hata oluştu", result.cause().getMessage());
                message.reply(errorResponse.toJson());
            }
        });
    }
}
