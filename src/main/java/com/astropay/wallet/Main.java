package com.astropay.wallet;

public class Main {
    public static void main(String[] args) {
        Deposit deposit = new Deposit();
        deposit.setSandbox(true);
        deposit.setApiKey("pXW9MPob1bdzb8nt20cfmMlZACTA1EPXlaMXtJVbAA0vgt8XbNATb8VK2QcirC6F");
        deposit.setSecretKey("QK4BPDCCCYP2WVBR6XRNYBSJED3XFJL6");

        deposit.init();
    }
}
