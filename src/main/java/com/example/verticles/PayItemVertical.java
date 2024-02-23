package com.example.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayItemVertical extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(PayItemVertical.class);

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();


        eventBus.consumer("pay_item.added", message -> {
            // Gelen mesajın içeriğini JsonObject olarak al
            JsonObject eventData = (JsonObject) message.body();


            logger.info("payitem item added - Type: {}, amount: {}",
                    eventData.getString("type"),
                    eventData.getDouble("amount"));

        });

        logger.info("PayVerticle started and listening for 'sale.item.added' events.");
    }
}
