package com.example.constants;

public class PayItemConst {
    public  static final  String SQLSELECT="SELECT * FROM pay_item";
    public  static final  String SQLINSERT="INSERT INTO pay_item ( type, amount) VALUES ( ?, ?)";
    public  static final  String SQLDELETE= "DELETE FROM pay_item WHERE payId = ?";
    public  static final  String SQLUPDATE="UPDATE pay_item SET type = ?, amount = ? WHERE payId = ?";
    public  static final  String SQLCHECK="SELECT COUNT(*) FROM pay_item WHERE type=? AND amount=?  ";

    public static final String ITEMADD = "itemADD";
    public static  final String ITEMLIST="itemList";
    public static  final String ITEMPUT="itemPUT";
    public static  final String ITEMDELETE="itemDelete";
    public static  final String TABLE_NAME="pay_item";
}
