package com.example.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SaleItemAddedVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(SaleItemAddedVerticle.class);

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();

        // "sale.item.added" olayını dinle
        eventBus.consumer("sale_item.added", message -> {

            JsonObject eventData = (JsonObject) message.body();


            logger.info("Sale item added - Item Name: {}, VAT: {}, Price: {}",
                    eventData.getString("itemName"),
                    eventData.getDouble("vat"),
                    eventData.getDouble("price"));
        });
        logger.info("SaleItemAddedVerticle started and listening for 'sale.item.added' events.");
    }
}
