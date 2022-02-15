package com.astropay.wallet;

import java.math.BigDecimal;
import java.util.Currency;

public class MerchantBackend implements DepositResultListener {
    public void init() {
        Product product = new Product("mmc", "product_merchant_code", "product description");
        User user = new User("alvarodoune+sandbox@gmail.com");
        VisualInfo visualInfo = new VisualInfo("AstroPay", "https://getapp.astropaycard.com/img/astropay-logo.png");

        Deposit deposit = new Deposit();
        deposit.setSandbox(true);
        deposit.setApiKey("pXW9MPob1bdzb8nt20cfmMlZACTA1EPXlaMXtJVbAA0vgt8XbNATb8VK2QcirC6F");
        deposit.setSecretKey("QK4BPDCCCYP2WVBR6XRNYBSJED3XFJL6");
        deposit.setAmount(new BigDecimal("26.65"));
        deposit.setCurrency(Currency.getInstance("USD"));
        deposit.setCountry("UY");
        deposit.setMerchant_deposit_id("TEST-DEPOSIT-027");
        deposit.setCallback_url("https://www.booster.software");
        deposit.setUser(user);
        deposit.setProduct(product);
        deposit.setVisualInfo(visualInfo);
        deposit.registerDepositResultEventListener(this);

        deposit.init();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        deposit.checkDepositStatus();
    }

    @Override
    public void OnDepositSuccess(DepositResponse depositResponse) {
        System.out.println("Deposit Success!");
        System.out.println(depositResponse.getStatus());
        System.out.println(depositResponse.getUrl());
    }

    @Override
    public void OnDepositError(DepositResponse depositResponse) {
        System.out.println("An error occurred");
        System.out.println(depositResponse.getError() + "," + depositResponse.getDescription());
    }
}
