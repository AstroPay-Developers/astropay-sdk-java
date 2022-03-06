package com.astropay.resources;

import com.astropay.AstroPay;
import com.astropay.exceptions.APException;
import com.astropay.utils.Hmac;
import com.astropay.utils.SDKProperties;
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

public class CashoutV1 {
    private BigDecimal amount;
    private String currency;
    private String country;
    private String merchantCashoutId;
    private URL callbackUrl;
    private final User user;

    public CashoutV1(User user) {
        this.user = user;
    }

    public void init() throws APException {
        if (AstroPay.Sdk.getApiKey() == null || AstroPay.Sdk.getSecretKey() == null) {
            throw new APException("You must provide API-Key and Secret Key");
        }
        String cashoutURL = SDKProperties.getCashoutV1RequestURL();

        String jsonRequest = this.buildCashoutRequest();
        String hash = null;
        try {
            hash = Hmac.toHexString(Hmac.calcHmacSha256(AstroPay.Sdk.getSecretKey().getBytes(StandardCharsets.UTF_8), jsonRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new APException(this.merchantCashoutId, "There was an error in the method signature");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(cashoutURL)).timeout(Duration.ofMinutes(SDKProperties.getRequestTimeOutInMinutes())).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey(), "Signature", hash).POST(HttpRequest.BodyPublishers.ofString(jsonRequest)).build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(SDKProperties.getResponseTimeOutInSeconds(), TimeUnit.SECONDS);
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
        CashoutV1Response cashoutV1Response = g.fromJson(result, CashoutV1Response.class);

        if (cashoutV1Response.getError() != null) {
            AstroPay.Sdk.OnCashoutV1Error(cashoutV1Response);
        } else {
            AstroPay.Sdk.OnCashoutV1Success(cashoutV1Response);
        }
    }

    private String buildCashoutRequest() {
        Gson gson = new Gson();
        CashoutRequest cashoutRequest = new CashoutRequest();
        cashoutRequest.amount = amount.toString();
        cashoutRequest.currency = currency;
        cashoutRequest.country = country;
        cashoutRequest.merchant_cashout_id = merchantCashoutId;
        cashoutRequest.callback_url = callbackUrl.toString();
        cashoutRequest.user = new UserRequest();
        cashoutRequest.user.user_id = user.getUserId();
        cashoutRequest.user.merchant_user_id = user.getMerchantUserId();
        cashoutRequest.user.document = user.getDocument();
        cashoutRequest.user.document_type = user.getDocumentType() != null ? user.getDocumentType().toString() : null;
        cashoutRequest.user.email = user.getEmail();
        cashoutRequest.user.phone = user.getPhone();
        cashoutRequest.user.first_name = user.getFirstName();
        cashoutRequest.user.last_name = user.getLastName();
        cashoutRequest.user.birth_date = user.getBirthDate() != null ? user.getBirthDate().toString() : null;
        cashoutRequest.user.country = user.getCountry();

        return gson.toJson(cashoutRequest, CashoutRequest.class);
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
