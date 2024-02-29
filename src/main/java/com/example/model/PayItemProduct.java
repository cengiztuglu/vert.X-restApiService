package com.example.model;

import io.vertx.core.json.JsonObject;

public class PayItemProduct {
    private int payId;
    private String type;
    private double amount;

    public PayItemProduct(String type, Double amount) {
        this.type = type;
        this.amount = amount;
    }



    public int getPayId() {
        return payId;
    }

    public void setPayId(int payId) {
        this.payId = payId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public static PayItemProduct fromJson(JsonObject json) {
        if (json == null) {
            return null;
        }

        String type = json.getString("type");
        Double amount = json.getDouble("amount");

        if (type == null || amount == null) {
            return null;
        }

        return new PayItemProduct(type, amount);
    }
}
