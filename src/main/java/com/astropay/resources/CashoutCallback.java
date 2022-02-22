package com.astropay.resources;

import java.util.Date;

public class CashoutCallback {
    private int cashout_id;
    private String merchant_cashout_id;
    private String cashout_user_id;
    private String merchant_user_id;
    private String status;
    private Date end_status_date;

    public int getCashout_id() {
        return cashout_id;
    }

    public String getMerchant_cashout_id() {
        return merchant_cashout_id;
    }

    public String getCashout_user_id() {
        return cashout_user_id;
    }

    public String getMerchant_user_id() {
        return merchant_user_id;
    }

    public String getStatus() {
        return status;
    }

    public Date getEnd_status_date() {
        return end_status_date;
    }
}
