package com.astropay.resources;

import com.astropay.AstroPay;
import com.astropay.exceptions.APException;
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
    private BigDecimal amount;
    private String currency;
    private String country;
    private String merchantDepositId;
    private String depositExternalId;
    private URL callbackUrl;
    private URL redirectUrl; //optional
    private User user;
    private Product product;
    private VisualInfo visualInfo; //optional
    private static final String requestURL = "https://%env.astropay.com/merchant/v1/deposit/init";
    private static final String getStatusURL = "https://%env.astropay.com/merchant/v1/deposit/%deposit_external_id/status";

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
        String depositURL = requestURL.replace("%env", this.sandbox ? "onetouch-api-sandbox" : "onetouch-api");

        String bodyRequest = "{" +
                "\"amount\": " + this.amount + "," +
                "\"currency\": \"" + this.currency + "\"," +
                "\"country\": \"" + this.country + "\"," +
                "\"merchant_deposit_id\": \"" + this.merchantDepositId + "\"," +
                "\"callback_url\": \"" + this.callbackUrl + "\"," +
                "\"redirect_url\": \" " + this.redirectUrl + "\"," +
                "\"user\": {\n" +
                "    \"merchant_user_id\": \"" + this.user.getMerchantUserId() +
                "\"}\n," +
                "\"product\": {\n" +
                "    \"mcc\": \"" + this.product.getMcc() + "\",\n" +
                "    \"category\": \"" + this.product.getCategory() + "\",\n" +
                "    \"merchant_code\": \"" + this.product.getMerchantCode() + "\",\n" +
                "    \"description\": \"" + this.product.getDescription() + "\"\n" +
                "    },\n" +
                "\"visual_info\": {\n" +
                "    \"merchant_name\": \"" + this.visualInfo.getMerchantName() + "\",\n" +
                "    \"merchant_logo\": \"" + this.visualInfo.getMerchantLogo() + "\"\n" +
                "    }\n" +
                "}";
        String hash = null;
        try {
            hash = toHexString(calcHmacSha256(AstroPay.Sdk.getSecretKey().getBytes(StandardCharsets.UTF_8), bodyRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new APException(this.getMerchantDepositId(), "There was an error in the method signature");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(depositURL)).timeout(Duration.ofMinutes(2)).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey(), "Signature", hash).POST(HttpRequest.BodyPublishers.ofString(bodyRequest)).build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
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

        // check if listener is registered.
        if (this.depositResultListener != null) {
            if (depositResponse.getError() != null) {
                depositResultListener.OnDepositError(depositResponse);
            } else {
                this.depositExternalId = depositResponse.getDepositExternalId();
                depositResultListener.OnDepositSuccess(depositResponse);
            }
        }
    }

    /**
     * Request made in order to find out the status of a deposit
     *
     * @param deposit_external_id Deposit external ID
     */
    public void checkDepositStatus(String deposit_external_id) {
        String statusURL = getStatusURL.replace("%env", this.sandbox ? "onetouch-api-sandbox" : "onetouch-api");
        statusURL = statusURL.replace("%deposit_external_id", deposit_external_id);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest get = HttpRequest.newBuilder().uri(URI.create(statusURL)).timeout(Duration.ofMinutes(2)).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey()).GET().build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(get, HttpResponse.BodyHandlers.ofString());

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

    /**
     * Register listener
     *
     * @param mListener Deposit Result Listener
     */
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

    /**
     * @return merchant_deposit_id Unique identifier of transaction
     */
    public String getMerchantDepositId() {
        return merchantDepositId;
    }

    //region Setters

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

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
