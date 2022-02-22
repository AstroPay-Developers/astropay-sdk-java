package com.astropay.resources;

public class CashoutResponse {
    private String status;
    private int cashout_id;
    private String error;
    private String description;

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public int getCashoutId() {
        return cashout_id;
    }
}
