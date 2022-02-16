package com.astropay;

public class AstroPay {
    public static class Sdk {
        private static volatile String secretKey = null;

        public static void setSecretKey(String secretKey) {
            Sdk.secretKey = secretKey;
        }

        public static String getSecretKey() {
            return secretKey;
        }
    }
}
