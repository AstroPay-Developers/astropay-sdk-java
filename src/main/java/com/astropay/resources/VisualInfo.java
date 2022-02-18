package com.astropay.resources;

public class VisualInfo {
    private final String merchantName;
    private final String merchantLogo;

    public VisualInfo(String merchantName, String merchantLogo) {
        this.merchantName = merchantName;
        this.merchantLogo = merchantLogo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantLogo() {
        return merchantLogo;
    }
}
