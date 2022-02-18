package com.astropay;

public class AstroPay {
    public static class Sdk {
        private static volatile String secretKey = null;
        private static volatile String apiKey = null;

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
    }
}
