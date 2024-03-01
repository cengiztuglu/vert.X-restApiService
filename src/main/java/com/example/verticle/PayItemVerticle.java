package com.example.verticle;

import com.example.Constant;
import com.example.MySQLManager;
import com.example.Response;
import com.example.model.PayItemProduct;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.List;


public class PayItemVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(Constant.ITEMLIST,this::handleGetPayItems);

        eventBus.consumer(Constant.ITEMADD, this::handleAddPayItem);
        eventBus.consumer(Constant.ITEMPUT,this::handleUpdatePayItem);
        eventBus.consumer(Constant.ITEMDELETE,this::handleDeletePayItem);
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

    private void handleGetPayItems(Message<String> message) {
        MySQLManager.getInstance().getPayItemProductsFromDatabase(result -> {
            if (result.succeeded()) {
                List<PayItemProduct> payItemProducts = result.result();
                message.reply(Json.encode(payItemProducts));
            } else {
                message.fail(500, "");
            }
        });
    }

    private void handleUpdatePayItem(Message<String> message) {
        String payItemJsonString = message.body();
        JsonObject payItemJson = new JsonObject(payItemJsonString);

        MySQLManager.getInstance().updatePayItemProduct(PayItemProduct.fromJson(payItemJson), result -> {
            if (result.succeeded()) {
                Response successResponse = new Response(0, "PayItemProduct Güncelleme başarılı", "PayItemProduct ID: " + result.result());
                message.reply(successResponse.toJson());
            } else {
                Response errorResponse = new Response(500, "PayItemProduct güncelleme sırasında bir hata oluştu", result.cause().getMessage());
                message.reply(errorResponse.toJson());
            }
        });
    }



    private void handleDeletePayItem(Message<String> message) {
        String payItemIdString = message.body();

        try {
            JsonObject payItemJson = new JsonObject(payItemIdString);

            int payItemId = payItemJson.getInteger("payId");

            MySQLManager.getInstance().deletePayItem(payItemId, result -> {
                if (result.succeeded()) {
                    Response successResponse = new Response(0, "PayItemProduct Silme başarılı", "PayItemProduct ID: " + payItemId);
                    message.reply(successResponse.toJson());
                } else {
                    Response errorResponse = new Response(500, "PayItemProduct silme sırasında bir hata oluştu", result.cause().getMessage());
                    message.reply(errorResponse.toJson());
                }
            });
        } catch (DecodeException e) {
            Response errorResponse = new Response(400, "Geçersiz JSON formatı", e.getMessage());
            message.reply(errorResponse.toJson());
        }
    }


}





