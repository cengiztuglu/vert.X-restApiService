package com.example;

public class Response {
    private int responseCode;
    private String responseDescription;
    private String responseDetail;

    public Response(int responseCode, String responseDescription, String responseDetail) {
        this.responseCode = responseCode;
        this.responseDescription = responseDescription;
        this.responseDetail = responseDetail;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public String getResponseDetail() {
        return responseDetail;
    }

    // toJson metodunu ekleyelim
    public String toJson() {
        return "{" +
                "\"responseCode\":\"" + responseCode + "\"," +
                "\"responseDescription\":\"" + responseDescription + "\"," +
                "\"responseDetail\":\"" + responseDetail + "\"" +
                "}";
    }
}
