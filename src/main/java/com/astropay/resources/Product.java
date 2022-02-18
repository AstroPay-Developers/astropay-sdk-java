package com.astropay.resources;

public class Product {
    private final String mcc;
    private final String merchantCode;
    private final String description;
    private String category; //optional

    public Product(String mcc, String merchantCode, String description) {
        this.mcc = mcc;
        this.merchantCode = merchantCode;
        this.description = description;
    }

    public String getMcc() {
        return mcc;
    }

    public String getCategory() {
        return category;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public String getDescription() {
        return description;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
