package com.astropay.resources;

public class DepositRequest {
    public String amount;
    public String currency;
    public String country;
    public String merchant_deposit_id;
    public String callback_url;
    public String redirect_url;
    public UserRequest user;
    public ProductRequest product;
    public VisualInfoRequest visual_info;
}

class UserRequest {
    public String user_id;
    public String merchant_user_id;
    public String document;
    public String document_type;
    public String email;
    public String phone;
    public String first_name;
    public String last_name;
    public String birth_date;
    public String country;
}

class ProductRequest {
    public String mcc;
    public String category;
    public String merchant_code;
    public String description;
}

class VisualInfoRequest {
    public String merchant_name;
    public String merchant_logo;
}
