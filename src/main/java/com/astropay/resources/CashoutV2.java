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

public class CashoutV2 {
    private BigDecimal amount;
    private String currency;
    private String country;
    private String merchantCashoutId;
    private URL callbackUrl;
    private final User user;
    private URL redirectUrl; //optional
    private VisualInfo visualInfo; //optional
    private CashoutOnHold security; //optional

    public CashoutV2(User user) {
        this.user = user;
    }

    public void init() throws APException {
        if (AstroPay.Sdk.getApiKey() == null || AstroPay.Sdk.getSecretKey() == null) {
            throw new APException("You must provide API-Key and Secret Key");
        }
        String cashoutURL = SDKProperties.getCashoutV2RequestURL();

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
        CashoutV2Response cashoutResponse = g.fromJson(result, CashoutV2Response.class);

        if (cashoutResponse.getError() != null) {
            AstroPay.Sdk.OnCashoutV2Error(cashoutResponse);
        } else {
            AstroPay.Sdk.OnCashoutV2Success(cashoutResponse);
        }
    }

    private String buildCashoutRequest() {
        Gson gson = new Gson();
        CashoutV2Request cashoutV2Request = new CashoutV2Request();
        cashoutV2Request.amount = amount.toString();
        cashoutV2Request.currency = currency;
        cashoutV2Request.country = country;
        cashoutV2Request.merchant_cashout_id = merchantCashoutId;
        cashoutV2Request.callback_url = callbackUrl != null ? callbackUrl.toString() : null;
        cashoutV2Request.user = new UserRequest();
        cashoutV2Request.user.user_id = user.getUserId();
        cashoutV2Request.user.merchant_user_id = user.getMerchantUserId();
        cashoutV2Request.user.document = user.getDocument();
        cashoutV2Request.user.document_type = user.getDocumentType() != null ? user.getDocumentType().toString() : null;
        cashoutV2Request.user.email = user.getEmail();
        cashoutV2Request.user.phone = user.getPhone();
        cashoutV2Request.user.first_name = user.getFirstName();
        cashoutV2Request.user.last_name = user.getLastName();
        cashoutV2Request.user.birth_date = user.getBirthDate() != null ? user.getBirthDate().toString() : null;
        cashoutV2Request.user.country = user.getCountry();
        cashoutV2Request.redirect_url = redirectUrl != null ? redirectUrl.toString() : null;
        if (visualInfo != null) {
            cashoutV2Request.visual_info = new VisualInfoRequest();
            cashoutV2Request.visual_info.merchant_name = visualInfo.getMerchantName();
            cashoutV2Request.visual_info.merchant_logo = visualInfo.getMerchantLogo().toString();
        }
        if (security != null) {
            cashoutV2Request.security = new CashoutOnHoldRequest();
            cashoutV2Request.security.create_on_hold = security.createOnHold;
            cashoutV2Request.security.on_hold_confirmation_url = security.onHoldConfirmationUrl;
        }

        return gson.toJson(cashoutV2Request, CashoutV2Request.class);
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

    public void setRedirectUrl(URL redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setVisualInfo(VisualInfo visualInfo) {
        this.visualInfo = visualInfo;
    }

    public void setSecurity(CashoutOnHold security) {
        this.security = security;
    }
}
