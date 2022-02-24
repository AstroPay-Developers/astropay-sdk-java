package com.astropay.resources;

public class CashoutV2Request extends CashoutRequest {
    public String redirect_url;
    public VisualInfoRequest visual_info;
    public CashoutOnHoldRequest security;
}

class CashoutOnHoldRequest {
    public Boolean create_on_hold;
    public String on_hold_confirmation_url;
}

class ConfirmCashoutOnHoldRequest {
    public String cashout_external_id;
    public Boolean approve;
}
