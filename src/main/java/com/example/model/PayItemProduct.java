package com.example.model;

public class PayItemProduct {
    private int payId;
    private String type;
    private double amount;

    public PayItemProduct() {
    }

    public PayItemProduct(int payId, String type, double amount) {
        this.payId = payId;
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


}
