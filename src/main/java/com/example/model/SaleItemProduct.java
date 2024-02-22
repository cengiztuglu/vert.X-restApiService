package com.example.model;


public class SaleItemProduct {

    private int itemId;
    private String itemName;
    private double vat;
    private double price;


    public SaleItemProduct() {
    }

    // Parametreli kurucu metod
    public SaleItemProduct(int itemId, String itemName, double vat, double price) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.vat = vat;
        this.price=price;
    }

    // Getter ve Setter metotları
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
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
    public double getPrice(){
        return price;
    }
    public void setPrice(double price)
    {
        this.price=price;
    }

    // toString metodu (isteğe bağlı, nesneyi string olarak temsil etmek için)
    @Override
    public String toString() {
        return "Product{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", price=" + price+
                ", vat=" + vat +
                '}';
    }
}


