package com.astropay;

public class Astropay {
    public static class SDK {
        private static volatile String secretKey = null;

        public static void setSecretKey(String secretKey) {
            SDK.secretKey = secretKey;
        }

        public static String getSecretKey() {
            return secretKey;
        }
    }
}
