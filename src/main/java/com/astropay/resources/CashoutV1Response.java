package com.astropay.resources;

public class CashoutV1Response extends ErrorResponse {
    private String status;
    private int cashout_id;

    public String getStatus() {
        return status;
    }

    public int getCashoutId() {
        return cashout_id;
    }
}
