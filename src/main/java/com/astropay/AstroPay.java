package com.astropay;

import com.astropay.resources.CashoutV1Response;
import com.astropay.resources.CashoutV1ResultListener;
import com.astropay.resources.DepositResponse;
import com.astropay.resources.DepositResultListener;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        private static final String depositURL = "https://%env.astropay.com/merchant/v1/deposit/init";
        private static final String depositStatusURL = "https://%env.astropay.com/merchant/v1/deposit/%deposit_external_id/status";
        private static final String cashoutV1StatusURL = "https://%env.astropay.com/merchant/v1/cashout/%cashout_id/status";
        private static final String cashoutV2StatusURL = "https://%env.astropay.com/merchant/v2/cashout/%cashout_external_id%/status";
        private static DepositResultListener depositResultListener;
        private static CashoutV1ResultListener cashoutV1ResultListener;

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

        public static String getDepositURL() {
            return depositURL.replace("%env", sandboxMode ? "onetouch-api-sandbox" : "onetouch-api");
        }

        public static String getDepositStatusURL() {
            return depositStatusURL.replace("%env", sandboxMode ? "onetouch-api-sandbox" : "onetouch-api");
        }

        public static String getCashoutV1StatusURL() {
            return cashoutV1StatusURL.replace("%env", sandboxMode ? "onetouch-api-sandbox" : "onetouch-api");
        }

        public static String getCashoutV2StatusURL() {
            return cashoutV2StatusURL.replace("%env", sandboxMode ? "onetouch-api-sandbox" : "onetouch-api");
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
            String getStatusURL = getDepositStatusURL().replace("%deposit_external_id", deposit_external_id);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest get = HttpRequest.newBuilder().uri(URI.create(getStatusURL)).timeout(Duration.ofMinutes(2)).headers("Content-Type", "application/json", "Merchant-Gateway-Api-Key", AstroPay.Sdk.getApiKey()).GET().build();

            CompletableFuture<HttpResponse<String>> response = client.sendAsync(get, HttpResponse.BodyHandlers.ofString());

            String result = null;
            try {
                result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
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
            String statusURL = AstroPay.Sdk.getCashoutV1StatusURL();
            statusURL = statusURL.replace("%cashout_id", cashout_id.toString());
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
    }
}
