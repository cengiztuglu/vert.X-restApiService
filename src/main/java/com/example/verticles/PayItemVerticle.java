package com.example.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class PayItemVerticle extends AbstractVerticle {

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();


        eventBus.consumer("sale.item.added", message -> {
            // Gelen mesajın içeriğini JsonObject olarak al
            JsonObject eventData = (JsonObject) message.body();


            System.out.println("Sale item added - Item Type: " + eventData.getString("type") +
                    ", Amount: " + eventData.getDouble("amount") );
        });

        System.out.println("PayVerticle started and listening for 'sale.item.added' events.");
    }
}
