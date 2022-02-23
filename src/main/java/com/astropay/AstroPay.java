package com.astropay;

import com.astropay.resources.DepositResponse;
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
        private static final String statusURL = "https://%env.astropay.com/merchant/v1/deposit/%deposit_external_id/status";

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

        public static String getStatusURL() {
            return statusURL.replace("%env", sandboxMode ? "onetouch-api-sandbox" : "onetouch-api");
        }

        /**
         * Request made in order to find out the status of a deposit
         *
         * @param deposit_external_id Deposit external ID
         */
        public static DepositResponse checkDepositStatus(String deposit_external_id) {
            String getStatusURL = getStatusURL().replace("%deposit_external_id", deposit_external_id);
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
            return g.fromJson(result, DepositResponse.class);
        }
    }
}
