package com.astropay.resources;

import java.net.URL;

public class VisualInfo {
    private final String merchantName;
    private final URL merchantLogo;

    public VisualInfo(String merchantName, URL merchantLogo) {
        this.merchantName = merchantName;
        this.merchantLogo = merchantLogo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public URL getMerchantLogo() {
        return merchantLogo;
    }
}
