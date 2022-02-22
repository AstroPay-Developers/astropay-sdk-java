package com.astropay.resources;

import com.astropay.AstroPay;
import com.astropay.exceptions.APException;
import com.astropay.utils.Hmac;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Cashout {
    private boolean sandbox = false;
    private CashoutResultListener cashoutResultListener;
    private BigDecimal amount;
    private String currency;
    private String country;
    private String merchantCashoutId;
    private URL callbackUrl;
    private final User user;
    private static final String requestURL = "https://%env.astropay.com/merchant/v1/cashout";

    public Cashout(User user) {
        this.user = user;
    }

    public void init() throws APException {
        if (AstroPay.Sdk.getApiKey() == null || AstroPay.Sdk.getSecretKey() == null) {
            throw new APException("You must provide API-Key and Secret Key");
        }
        String cashoutURL = requestURL.replace("%env", this.sandbox ? "onetouch-api-sandbox" : "onetouch-api");

        String bodyRequest = "{" +
                "\"amount\": " + this.amount + "," +
                "\"currency\": \"" + this.currency + "\"," +
                "\"country\": \"" + this.country + "\"," +
                "\"merchant_cashout_id\": \"" + this.merchantCashoutId + "\"," +
                "\"callback_url\": \"" + this.callbackUrl + "\"," +
                "\"user\": {\n" +
                "    \"merchant_user_id\": \"" + this.user.getMerchantUserId() +
                "\"}\n" +
                "}";
        String hash = null;
        try {
            hash = Hmac.toHexString(Hmac.calcHmacSha256(AstroPay.Sdk.getSecretKey().getBytes(StandardCharsets.UTF_8), bodyRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new APException(this.merchantCashoutId, "There was an error in the method signature");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(cashoutURL)).timeout(Duration.ofMinutes(2)).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey(), "Signature", hash).POST(HttpRequest.BodyPublishers.ofString(bodyRequest)).build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new APException(this.merchantCashoutId, "Thread is interrupted, either before or during the activity");
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new APException(this.merchantCashoutId, "ExecutionException caused by: " + e.getCause(), e);
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new APException(this.merchantCashoutId, "Timeout Exception", e);
        }

        Gson g = new Gson();
        CashoutResponse cashoutResponse = g.fromJson(result, CashoutResponse.class);

        // check if listener is registered.
        if (this.cashoutResultListener != null) {
            if (cashoutResponse.getError() != null) {
                cashoutResultListener.OnCashoutError(cashoutResponse);
            } else {
//                this.depositExternalId = cashoutResponse.getDepositExternalId();
                cashoutResultListener.OnCashoutSuccess(cashoutResponse);
            }
        }
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public void setCashoutResultListener(CashoutResultListener cashoutResultListener) {
        this.cashoutResultListener = cashoutResultListener;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setMerchantCashoutId(String merchantCashoutId) {
        this.merchantCashoutId = merchantCashoutId;
    }

    public void setCallbackUrl(URL callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
