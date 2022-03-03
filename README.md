# astropay-sdk-java

Simple usage looks like:
``` java
import com.astropay.AstroPay;
import com.astropay.exceptions.APException;
import com.astropay.resources.*;

public class Main {

    public static void main(String[] args) {
        AstroPay.Sdk.setSecretKey("YOUR_SECRET_KEY");
        AstroPay.Sdk.setApiKey("YOUR_API_KEY");
        AstroPay.Sdk.registerDepositResultEventListener(this);
        AstroPay.Sdk.setSandboxMode(true); //optional, default false

        URL callbackUrl = buildURL("https://your-callback-url");

        Product product = new Product("7995", "product_merchant_code", "product description");

        User user = new User("exampleuser@example.com");

        deposit = new Deposit(user, product);
        deposit.setAmount(new BigDecimal("29.77"));
        deposit.setCurrency(Currencies.USD);
        deposit.setCountry(Countries.Uruguay);
        deposit.setMerchantDepositId("merchant_deposit_id");
        deposit.setCallbackUrl(callbackUrl);

        deposit.init();


        //result listeners
        @Override
        public void OnDepositSuccess(DepositResponse depositResponse) {
            System.out.println(depositResponse.getStatus() + "\n" + depositResponse.getUrl());
        }

        @Override
        public void OnDepositError(DepositResponse depositResponse) {
            System.out.println(depositResponse.getError() + ", " + depositResponse.getDescription());
        }

        @Override
        public void OnStatusResult(DepositResponse depositResponse) {
            if (depositResponse.getError() != null) {
                System.out.println(depositResponse.getError());
            } else {
                System.out.println(depositResponse.getStatus());
            }
        }
    }
}
```
