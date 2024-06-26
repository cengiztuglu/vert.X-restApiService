package com.example.model;

import io.vertx.core.json.JsonObject;

public class PayItemProduct {
    private Integer payId;
    private String type;
    private Double amount;

    public PayItemProduct()
    {

    }
    public PayItemProduct(String type,Integer payId, Double amount) {
        this.type = type;
        this.payId=payId;
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
       Integer payId = json.getInteger("itemId");


        if (type == null || amount == null) {
            return null;
        }

        return new PayItemProduct(type,payId,amount);
    }
}
