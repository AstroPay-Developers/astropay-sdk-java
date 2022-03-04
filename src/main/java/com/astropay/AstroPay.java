package com.astropay;

import com.astropay.resources.*;
import com.astropay.utils.Hmac;
import com.astropay.utils.SDKProperties;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AstroPay {
    public static class Sdk {
        private static volatile Boolean sandboxMode = false;
        private static volatile String secretKey = null;
        private static volatile String apiKey = null;
        private static DepositResultListener depositResultListener;
        private static CashoutV1ResultListener cashoutV1ResultListener;
        private static CashoutV2ResultListener cashoutV2ResultListener;
        public static final SDKProperties sdkProperties = new SDKProperties();

        public static void setSandboxMode(Boolean sandboxMode) {
            Sdk.sandboxMode = sandboxMode;
        }

        public static void setSecretKey(String secretKey) {
            Sdk.secretKey = secretKey;
        }

        public static void setApiKey(String apiKey) {
            Sdk.apiKey = apiKey;
        }

        public static String getSecretKey() {
            return secretKey;
        }

        public static String getApiKey() {
            return apiKey;
        }

        public static Boolean getSandboxMode() {
            return sandboxMode;
        }

        //region Deposits

        public static void OnDepositStatusResult(DepositResponse result) {
            if (depositResultListener != null) {
                depositResultListener.OnStatusResult(result);
            }
        }

        public static void OnDepositError(DepositResponse result) {
            if (depositResultListener != null) {
                depositResultListener.OnDepositError(result);
            }
        }

        public static void OnDepositSuccess(DepositResponse result) {
            if (depositResultListener != null) {
                depositResultListener.OnDepositSuccess(result);
            }
        }

        /**
         * Request made in order to find out the status of a deposit
         *
         * @param deposit_external_id Deposit external ID
         */
        public static void checkDepositStatus(String deposit_external_id) {
            String getStatusURL = SDKProperties.getDepositStatusURL().replace("%deposit_external_id", deposit_external_id);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest get = HttpRequest.newBuilder().uri(URI.create(getStatusURL)).timeout(Duration.ofMinutes(SDKProperties.getRequestTimeOutInMinutes())).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey()).GET().build();

            CompletableFuture<HttpResponse<String>> response = client.sendAsync(get, HttpResponse.BodyHandlers.ofString());

            String result = null;
            try {
                result = response.thenApply(HttpResponse::body).get(SDKProperties.getResponseTimeOutInSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }

            Gson g = new Gson();
            if (depositResultListener != null) {
                depositResultListener.OnStatusResult(g.fromJson(result, DepositResponse.class));
            }
        }

        /**
         * Register listener
         *
         * @param mListener Deposit Result Listener
         */
        public static void registerDepositResultEventListener(DepositResultListener mListener) {
            depositResultListener = mListener;
        }

        //endregion deposits

        // region CashoutV1

        public static void OnCashoutV1StatusResult(CashoutV1Response result) {
            if (cashoutV1ResultListener != null) {
                cashoutV1ResultListener.OnCashoutStatusResult(result);
            }
        }

        public static void OnCashoutV1Error(CashoutV1Response result) {
            if (cashoutV1ResultListener != null) {
                cashoutV1ResultListener.OnCashoutError(result);
            }
        }

        public static void OnCashoutV1Success(CashoutV1Response result) {
            if (cashoutV1ResultListener != null) {
                cashoutV1ResultListener.OnCashoutSuccess(result);
            }
        }

        /**
         * Checking Cashout Status
         * If necessary, you can manually check a cashout status with this endpoint. Please note this is not required as a callback with the final status will be sent within 24h.
         *
         * @param cashout_id Cashout ID as Integer
         */
        public static void checkCashoutV1Status(Integer cashout_id) {
            String statusURL = SDKProperties.getCashoutV1StatusURL();
            statusURL = statusURL.replace("%cashout_id", cashout_id.toString());
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
            CashoutV1Response statusResponse = g.fromJson(result, CashoutV1Response.class);
            cashoutV1ResultListener.OnCashoutStatusResult(statusResponse);
        }

        /**
         * Register listener
         *
         * @param mListener CashoutV1 Result Listener
         */
        public static void registerCashoutV1ResultEventListener(CashoutV1ResultListener mListener) {
            cashoutV1ResultListener = mListener;
        }

        //endregion cashoutv1

        // region CashoutV2

        public static void OnCashoutV2StatusResult(CashoutV2Response result) {
            if (cashoutV2ResultListener != null) {
                cashoutV2ResultListener.OnCashoutStatusResult(result);
            }
        }

        public static void OnCashoutV2Error(CashoutV2Response result) {
            if (cashoutV2ResultListener != null) {
                cashoutV2ResultListener.OnCashoutError(result);
            }
        }

        public static void OnCashoutV2Success(CashoutV2Response result) {
            if (cashoutV2ResultListener != null) {
                cashoutV2ResultListener.OnCashoutSuccess(result);
            }
        }

        /**
         * Register listener
         *
         * @param mListener CashoutV2 Result Listener
         */
        public static void registerCashoutV2ResultEventListener(CashoutV2ResultListener mListener) {
            cashoutV2ResultListener = mListener;
        }

        /**
         * Checking Cashout Status
         * If necessary, you can manually check a cashout status with this endpoint. Please note this is not required as a callback with the final status will be sent within 24h.
         *
         * @param cashout_external_id Cashout external ID
         */
        public static void checkCashoutV2Status(String cashout_external_id) {
            String statusURL = SDKProperties.getCashoutV2StatusURL();
            statusURL = statusURL.replace("%cashout_external_id", cashout_external_id);
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
            CashoutV2Response statusResponse = g.fromJson(result, CashoutV2Response.class);
            cashoutV2ResultListener.OnCashoutStatusResult(statusResponse);
        }

        /**
         * Approve or Cancel a cashout that is on hold
         *
         * @param cashoutExternalId: String, approve: Boolean
         */
        public static int confirmCashoutOnHold(String cashoutExternalId, Boolean approve) {
            String confirmURL = SDKProperties.getCashoutV2ConfirmURL();
            Gson gson = new Gson();
            ConfirmCashoutOnHoldRequest cashoutOnHoldRequest = new ConfirmCashoutOnHoldRequest();
            cashoutOnHoldRequest.cashout_external_id = cashoutExternalId;
            cashoutOnHoldRequest.approve = approve;

            String jsonRequest = gson.toJson(cashoutOnHoldRequest, ConfirmCashoutOnHoldRequest.class);

            String hash = null;
            try {
                hash = Hmac.toHexString(Hmac.calcHmacSha256(AstroPay.Sdk.getSecretKey().getBytes(StandardCharsets.UTF_8), jsonRequest.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(confirmURL)).timeout(Duration.ofMinutes(SDKProperties.getRequestTimeOutInMinutes())).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey(), "Signature", hash).POST(HttpRequest.BodyPublishers.ofString(jsonRequest)).build();
            CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            return response.thenApply(HttpResponse::statusCode).join();
        }
    }
}
