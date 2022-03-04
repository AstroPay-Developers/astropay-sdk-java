package com.astropay.utils;

import com.astropay.AstroPay;

import java.io.*;
import java.util.Properties;

public class SDKProperties {
    private static String depositURL;
    private static String depositStatusURL;
    private static String cashoutV1RequestURL;
    private static String cashoutV1StatusURL;
    private static String cashoutV2RequestURL;
    private static String cashoutV2StatusURL;
    private static String cashoutV2ConfirmURL;


    public SDKProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            depositURL = properties.getProperty("depositURL");
            depositStatusURL = properties.getProperty("depositStatusURL");
            cashoutV1RequestURL = properties.getProperty("cashoutV1RequestURL");
            cashoutV1StatusURL = properties.getProperty("cashoutV1StatusURL");
            cashoutV2RequestURL = properties.getProperty("cashoutV2RequestURL");
            cashoutV2StatusURL = properties.getProperty("cashoutV2StatusURL");
            cashoutV2ConfirmURL = properties.getProperty("cashoutV2ConfirmURL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDepositURL() {
        return replaceEnvironment(depositURL);
    }

    public static String getDepositStatusURL() {
        return replaceEnvironment(depositStatusURL);
    }

    public static String getCashoutV1RequestURL() {
        return replaceEnvironment(cashoutV1RequestURL);
    }

    public static String getCashoutV1StatusURL() {
        return replaceEnvironment(cashoutV1StatusURL);
    }

    public static String getCashoutV2RequestURL() {
        return replaceEnvironment(cashoutV2RequestURL);
    }

    public static String getCashoutV2StatusURL() {
        return replaceEnvironment(cashoutV2StatusURL);
    }

    public static String getCashoutV2ConfirmURL() {
        return replaceEnvironment(cashoutV2ConfirmURL);
    }

    private static String replaceEnvironment(String url) {
        return url.replace("%env", AstroPay.Sdk.getSandboxMode() ? "onetouch-api-sandbox" : "onetouch-api");
    }
}
