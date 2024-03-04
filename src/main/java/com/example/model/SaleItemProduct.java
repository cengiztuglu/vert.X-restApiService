package com.example.model;

import io.vertx.core.json.JsonObject;

public class SaleItemProduct {
private Integer itemId;
    private String itemName;
    private Double vat;
    private Double price;

    public SaleItemProduct() {
    }

    public SaleItemProduct(Integer itemId,String itemName, Double vat, Double price) {
        this.itemId=itemId;
        this.itemName = itemName;
        this.vat = vat;
        this.price = price;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }


    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public static SaleItemProduct fromJson(JsonObject json) {
        if (json == null) {
            return null;
        }

        Integer itemId = json.getInteger("itemId");
        String itemName = json.getString("itemName");
        Double vat = json.getDouble("vat");
        Double price = json.getDouble("price");



        if (price == null || vat== null || itemName==null) {
            return null;
        }

        return new SaleItemProduct(itemId,itemName,vat,price);
    }


}
