package com.astropay.resources;

public class DepositResponse {
    private String status;
    private String url;
    private String merchant_deposit_id;
    private String deposit_external_id;
    private String error;
    private String description;

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public String getMerchantDepositId() {
        return merchant_deposit_id;
    }

    public String getDepositExternalId() {
        return deposit_external_id;
    }

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }
}
