package com.astropay.resources;

import com.astropay.AstroPay;
import com.astropay.exceptions.APException;
import com.astropay.utils.Hmac;
import com.astropay.utils.SDKProperties;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Deposit {
    private BigDecimal amount;
    private String currency;
    private String country;
    private String merchantDepositId;
    private String depositExternalId;
    private URL callbackUrl;
    private URL redirectUrl; //optional
    private VisualInfo visualInfo; //optional
    private final Product product;
    private final User user;

    public Deposit(User user, Product product) {
        this.user = user;
        this.product = product;
    }

    /**
     * Used to initiate a user deposit
     *
     * @throws APException APBase Exception
     */
    public void init() throws APException {
        if (AstroPay.Sdk.getApiKey() == null || AstroPay.Sdk.getSecretKey() == null) {
            throw new APException("You must provide API-Key and Secret Key");
        }
        String depositURL = SDKProperties.getDepositURL();
        String jsonRequest = this.buildDepositRequest();
        String hash = null;
        try {
            hash = Hmac.toHexString(Hmac.calcHmacSha256(AstroPay.Sdk.getSecretKey().getBytes(StandardCharsets.UTF_8), jsonRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new APException(this.getMerchantDepositId(), "There was an error in the method signature");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(depositURL)).timeout(Duration.ofMinutes(2)).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey(), "Signature", hash).POST(HttpRequest.BodyPublishers.ofString(jsonRequest)).build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(SDKProperties.getResponseTimeOutInSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new APException(this.getMerchantDepositId(), "Thread is interrupted, either before or during the activity");
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new APException(this.getMerchantDepositId(), "ExecutionException caused by: " + e.getCause(), e);
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new APException(this.getMerchantDepositId(), "Timeout Exception", e);
        }

        Gson g = new Gson();
        DepositResponse depositResponse = g.fromJson(result, DepositResponse.class);

        if (depositResponse.getError() != null) {
            AstroPay.Sdk.OnDepositError(depositResponse);
        } else {
            depositExternalId = depositResponse.getDepositExternalId();
            AstroPay.Sdk.OnDepositSuccess(depositResponse);
        }
    }

    /**
     * Request made in order to find out the status of a deposit
     *
     * @param deposit_external_id Deposit external ID
     */
    public void checkDepositStatus(String deposit_external_id) {
        String statusURL = SDKProperties.getDepositStatusURL();
        statusURL = statusURL.replace("%deposit_external_id", deposit_external_id);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest get = HttpRequest.newBuilder().uri(URI.create(statusURL)).timeout(Duration.ofMinutes(SDKProperties.getRequestTimeOutInMinutes())).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey()).GET().build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(get, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(SDKProperties.getResponseTimeOutInSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        Gson g = new Gson();
        DepositResponse statusResponse = g.fromJson(result, DepositResponse.class);

        AstroPay.Sdk.OnDepositStatusResult(statusResponse);
    }

    /**
     * @return merchant_deposit_id Unique identifier of transaction
     */
    public String getMerchantDepositId() {
        return merchantDepositId;
    }

    private String buildDepositRequest() {
        Gson gson = new Gson();
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.amount = amount.toString();
        depositRequest.currency = currency;
        depositRequest.country = country;
        depositRequest.merchant_deposit_id = merchantDepositId;
        depositRequest.callback_url = callbackUrl.toString();
        depositRequest.redirect_url = redirectUrl != null ? redirectUrl.toString() : null;
        depositRequest.user = new UserRequest();
        depositRequest.user.user_id = user.getUserId();
        depositRequest.user.merchant_user_id = user.getMerchantUserId();
        depositRequest.user.document = user.getDocument();
        depositRequest.user.document_type = user.getDocumentType() != null ? user.getDocumentType().toString() : null;
        depositRequest.user.email = user.getEmail();
        depositRequest.user.phone = user.getPhone();
        depositRequest.user.first_name = user.getFirstName();
        depositRequest.user.last_name = user.getLastName();
        depositRequest.user.birth_date = user.getBirthDate() != null ? user.getBirthDate().toString() : null;
        depositRequest.user.country = user.getCountry();
        depositRequest.product = new ProductRequest();
        depositRequest.product.mcc = product.getMcc();
        depositRequest.product.category = product.getCategory();
        depositRequest.product.merchant_code = product.getMerchantCode();
        depositRequest.product.description = product.getDescription();
        if (visualInfo != null) {
            depositRequest.visual_info = new VisualInfoRequest();
            depositRequest.visual_info.merchant_name = visualInfo.getMerchantName();
            depositRequest.visual_info.merchant_logo = visualInfo.getMerchantLogo().toString();
        }

        return gson.toJson(depositRequest, DepositRequest.class);
    }

    // region Setters

    /**
     * @param amount Deposit Amount as BigDecimal
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * @param currency Deposit Currency as String
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCountry(String country) {
        if (country.length() != 2) {
            throw new Error("Country must be String (2) ISO Code");
        }
        this.country = country;
    }

    public void setMerchantDepositId(String merchantDepositId) {
        this.merchantDepositId = merchantDepositId;
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
    //endregion
}
