package com.example.model;

public class SaleItemProduct {

    private String itemName;
    private double vat;
    private double price;

    public SaleItemProduct() {
    }

    // Parametreli kurucu metod
    public SaleItemProduct(String itemName, double vat, double price) {
        this.itemName = itemName;
        this.vat = vat;
        this.price = price;
    }

    // Getter ve Setter metotları
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

    // toString metodu (isteğe bağlı, nesneyi string olarak temsil etmek için)
    @Override
    public String toString() {
        return "Product{" +
                "itemName='" + itemName + '\'' +
                ", vat=" + vat +
                ", price=" + price +
                '}';
    }
}
