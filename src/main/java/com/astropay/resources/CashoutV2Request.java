package com.astropay.resources;

public class CashoutV2Request extends CashoutRequest {
    public String redirect_url;
    public VisualInfoRequest visual_info;
    public CashoutOnHoldRequest security;
}

/**
 * on_hold_confirmation_url is required if create_on_hold is set to true.
 */
class CashoutOnHoldRequest {
    public Boolean create_on_hold;
    public String on_hold_confirmation_url;
}

