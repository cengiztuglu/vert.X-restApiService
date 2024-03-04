package com.example.constants;

public class SaleItemConst {
    public  static final  String SQLSELECT="SELECT * FROM sale_item";
    public  static final  String SQLINSERT="INSERT INTO sale_item ( itemName, price,vat) VALUES ( ?, ?,?)";
    public  static final  String SQLDELETE= "DELETE FROM sale_item WHERE itemId = ?";
    public  static final  String SQLUPDATE="UPDATE pay_item SET itemName = ?, price = ?, vat=? WHERE itemId = ?";

    public static final String ITEMADD = "saleItemADD";
    public static  final String ITEMLIST="saleItemList";
    public static  final String ITEMPUT="saleItemPUT";
    public static  final String ITEMDELETE="saleItemDelete";
    public static  final String TABLE_NAME="sale_item";

}
