package com.astropay.wallet;

import java.util.Date;

public class User {
    private String merchant_user_id;
    private String user_id;
    private String document;
    private DocumentType documentType;
    private String email;
    private String phone;
    private String first_name;
    private String last_name;
    private Date birth_date;
    private String country;

    public User(String merchant_user_id) {
        this.merchant_user_id = merchant_user_id;
    }

    public String getMerchant_user_id() {
        return merchant_user_id;
    }

    public void setMerchant_user_id(String merchant_user_id) {
        this.merchant_user_id = merchant_user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Date getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(Date birth_date) {
        this.birth_date = birth_date;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
