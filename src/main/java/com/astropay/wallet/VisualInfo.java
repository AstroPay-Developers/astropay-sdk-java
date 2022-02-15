package com.astropay.wallet;

public class VisualInfo {
    private String merchant_name;
    private String merchant_logo;

    public VisualInfo(String merchant_name, String merchant_logo) {
        this.merchant_name = merchant_name;
        this.merchant_logo = merchant_logo;
    }

    public String getMerchant_name() {
        return merchant_name;
    }

    public String getMerchant_logo() {
        return merchant_logo;
    }
}
