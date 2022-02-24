package com.astropay.resources;

public class CashoutV2Response extends ErrorResponse {
    private String status;
    private String url;
    private String merchant_cashout_id;
    private String cashout_external_id;

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public String getMerchantCashoutId() {
        return merchant_cashout_id;
    }

    public String getCashoutExternalId() {
        return cashout_external_id;
    }
}
