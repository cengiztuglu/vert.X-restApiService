package com.example.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class SaleItemAddedVerticle extends AbstractVerticle {

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();

        // "sale.item.added" olayını dinle
        eventBus.consumer("sale.item.added", message -> {
            // Gelen mesajın içeriğini JsonObject olarak al
            JsonObject eventData = (JsonObject) message.body();

            // Veriyi konsola yazdır
            System.out.println("Sale item added - Item Name: " + eventData.getString("itemName") +
                    ", VAT: " + eventData.getDouble("vat") +
                    ", Price: " + eventData.getDouble("price"));
        });

        System.out.println("SaleItemAddedVerticle started and listening for 'sale.item.added' events.");
    }
}
