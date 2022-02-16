package com.astropay.resources;

import com.astropay.AstroPay;
import com.google.gson.Gson;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Currency;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Deposit {
    private DepositResultListener depositResultListener;
    private boolean sandbox = false;
    private String apiKey;
    private BigDecimal amount;
    private Currency currency;
    private String country;
    private String merchant_deposit_id;
    private String deposit_external_id;
    private String callback_url;
    private String redirect_url; //optional
    private User user;
    private Product product;
    private VisualInfo visualInfo; //optional
    private static final String requestURL = "https://%env.astropay.com/merchant/v1/deposit/init";
    private static final String getStatusURL = "https://%env.astropay.com/merchant/v1/deposit/%deposit_external_id/status";

    //using java.net.http.HttpClient
    public void init() {
        if (this.apiKey == null || AstroPay.Sdk.getSecretKey() == null) {
            throw new Error("You must provide API-Key and Secret Key");
        }
        String depositURL = requestURL.replace("%env", this.sandbox ? "onetouch-api-sandbox" : "onetouch-api");
        System.out.println("Calling AstroPay Deposit");

        String bodyRequest = "{" +
                "\"amount\": " + this.amount + "," +
                "\"currency\": \"" + this.getCurrency() + "\"," +
                "\"country\": \"" + this.country + "\"," +
                "\"merchant_deposit_id\": \"" + this.merchant_deposit_id + "\"," +
                "\"callback_url\": \"" + this.callback_url + "\"," +
                "\"redirect_url\": \" " + this.redirect_url + "\"," +
                "\"user\": {\n" +
                "    \"merchant_user_id\": \"" + this.user.getMerchant_user_id() +
                "\"}\n," +
                "\"product\": {\n" +
                "    \"mcc\": 7995,\n" +
                "    \"category\": \"test_deposit\",\n" +
                "    \"merchant_code\": \"test\",\n" +
                "    \"description\": \"wallet deposit\"\n" +
                "    },\n" +
                "\"visual_info\": {\n" +
                "    \"merchant_name\": \"" + this.visualInfo.getMerchant_name() + "\",\n" +
                "    \"merchant_logo\": \"https://getapp.astropaycard.com/img/astropay-logo.png\"\n" +
                "    }\n" +
                "}";
        String hash = null;
        try {
            hash = toHexString(calcHmacSha256(AstroPay.Sdk.getSecretKey().getBytes(StandardCharsets.UTF_8), bodyRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(depositURL))
                .timeout(Duration.ofMinutes(2))
                .headers(
                        "Content-Type", "application/json",
                        "Merchant-Gateway-Api-Key", this.apiKey,
                        "Signature", hash
                )
                .POST(HttpRequest.BodyPublishers.ofString(bodyRequest))
                .build();

        CompletableFuture<HttpResponse<String>> response =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        Gson g = new Gson();
        DepositResponse depositResponse = g.fromJson(result, DepositResponse.class);

        // check if listener is registered.
        if (this.depositResultListener != null) {
            if (depositResponse.getError() != null) {
                depositResultListener.OnDepositError(depositResponse);
            } else {
                this.deposit_external_id = depositResponse.getDeposit_external_id();
                depositResultListener.OnDepositSuccess(depositResponse);
            }
        }
    }

    public void checkDepositStatus() {
        String statusURL = getStatusURL.replace("%env", this.sandbox ? "onetouch-api-sandbox" : "onetouch-api");
        statusURL = statusURL.replace("%deposit_external_id", this.deposit_external_id);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(statusURL))
                .timeout(Duration.ofMinutes(2))
                .headers(
                        "Content-Type", "application/json",
                        "Merchant-Gateway-Api-Key", this.apiKey
                )
                .GET()
                .build();

        CompletableFuture<HttpResponse<String>> response =
                client.sendAsync(get, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        Gson g = new Gson();
        DepositResponse statusResponse = g.fromJson(result, DepositResponse.class);

        depositResultListener.OnStatusResult(statusResponse);
    }

    // setting the listener
    public void registerDepositResultEventListener(DepositResultListener mListener) {
        this.depositResultListener = mListener;
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);
        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));
        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    static public byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
        byte[] hmacSha256;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
        return hmacSha256;
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String encode(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return toHexString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    //region Setters
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setCountry(String country) {
        if (country.length() != 2) {
            throw new Error("Country must be String (2) ISO Code");
        }
        this.country = country;
    }

    public String getMerchant_deposit_id() {
        return merchant_deposit_id;
    }

    public void setMerchant_deposit_id(String merchant_deposit_id) {
        this.merchant_deposit_id = merchant_deposit_id;
    }

    public void setCallback_url(String callback_url) {
        this.callback_url = callback_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setVisualInfo(VisualInfo visualInfo) {
        this.visualInfo = visualInfo;
    }
    //endregion
}
