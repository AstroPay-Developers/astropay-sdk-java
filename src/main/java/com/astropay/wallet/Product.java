package com.astropay.wallet;

public class Product {
    private String mcc;
    private String category; //optional
    private String merchant_code;
    private String description;

    public Product(String mcc, String merchant_code, String description) {
        this.mcc = mcc;
        this.merchant_code = merchant_code;
        this.description = description;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMerchant_code() {
        return merchant_code;
    }

    public void setMerchant_code(String merchant_code) {
        this.merchant_code = merchant_code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
