package com.example;

public final class Constant {
    public static final String ITEMADD = "itemADD";
    public static  final String ITEMLIST="itemList";
    public static  final String ITEMPUT="itemPUT";
    public static  final String RESPONSECOD="responseCode";
    public static  final String RESPONDESC="responseDescription";
    public static  final String RESPONSEDETAIL="responseDetail";

    public static  final String CONTENT="content-type";
    public static  final String APPLICATION="application/json";
    public static  final String INTERNALS="Internal Server Error";
    public static  final String ERRORPARS= "Error parsing JSON response";
    public  static final  String SQLUPDATE="UPDATE pay_item SET type = ?, amount = ? WHERE payId = ?";
    public  static final  String SQLSELECT="SELECT * FROM pay_item";
    public  static final  String SQLINSERT="INSERT INTO pay_item ( type, amount) VALUES ( ?, ?)";

    public  static final  String SQLDELETE= "DELETE FROM pay_item WHERE payId = ?";










}
