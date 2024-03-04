package com.example.verticle;

import com.example.MySQLManager;
import com.example.Response;
import com.example.constants.PayItemConst;
import com.example.constants.SaleItemConst;
import com.example.model.PayItemProduct;
import com.example.model.SaleItemProduct;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class SaleItemVerticle extends AbstractVerticle
{
    @Override
    public void start(Promise<Void> startPromise) {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(SaleItemConst.ITEMLIST,this::handleGetSaleItems);

        eventBus.consumer(SaleItemConst.ITEMADD, this::handleAddSaleItem);
        eventBus.consumer(SaleItemConst.ITEMPUT,this::handleUpdateSaleItem);
        eventBus.consumer(SaleItemConst.ITEMDELETE,this::handleDeleteSaleItem);
    }


    private void handleAddSaleItem(Message<String> message) {
        String payItemJsonString = message.body();
        JsonObject saleItemJson = new JsonObject(payItemJsonString);

        MySQLManager.getInstance().addProductToDatabase(SaleItemProduct.fromJson(saleItemJson), result -> {
            if (result.succeeded()) {
                Response successResponse = new Response(0, "saleItemProduct ekleme başarılı", "saleItemProduct ID: " + result.result());
                message.reply(successResponse.toJson());
            } else {
                Response errorResponse = new Response(500, "saleItemProduct ekleme sırasında bir hata oluştu", result.cause().getMessage());
                message.reply(errorResponse.toJson());
            }
        });
    }

    private void handleGetSaleItems(Message<String> message) {
        MySQLManager.getInstance().getSaleItemProductsFromDatabase(result -> {
            if (result.succeeded()) {
                List<SaleItemProduct> saleItemProducts = result.result();
                message.reply(Json.encode(saleItemProducts));
            } else {
                message.fail(500, "");
            }
        });
    }

    private void handleUpdateSaleItem(Message<String> message) {
        String saleItemJsonString = message.body();

        JsonObject saleItemJson = new JsonObject(saleItemJsonString);

        Long saleItemId = saleItemJson.getLong("saleItemId");

        MySQLManager.getInstance().updateProductToDatabase(SaleItemProduct.fromJson(saleItemJson), result -> {
            if (result.succeeded()) {
                Response successResponse = new Response(0, "saleItemProduct Güncelleme başarılı", "PayItemProduct ID: " + saleItemId);
                message.reply(successResponse.toJson());
            } else {
                Response errorResponse = new Response(500, "saleItemProduct güncelleme sırasında bir hata oluştu", result.cause().getMessage());
                message.reply(errorResponse.toJson());
            }
        });
    }



    private void handleDeleteSaleItem(Message<String> message) {
        String saleItemIdString = message.body();
        try {
            JsonObject saleItemJson = new JsonObject(saleItemIdString);
            Long saleItemId = saleItemJson.getLong("saleItemId");

            MySQLManager.getInstance().deleteProductFromDatabase(saleItemId, SaleItemProduct.class, result -> {
                if (result.succeeded()) {
                    Response successResponse = new Response(0, "saleItemProduct Silme başarılı", "saleItemProduct ID: " + saleItemId);
                    message.reply(successResponse.toJson());
                } else {
                    Response errorResponse = new Response(500, "saleItemProduct silme sırasında bir hata oluştu", result.cause().getMessage());
                    message.reply(errorResponse.toJson());
                }
            });
        } catch (DecodeException e) {
            Response errorResponse = new Response(400, "Geçersiz JSON formatı", e.getMessage());
            message.reply(errorResponse.toJson());
        }
    }


}
