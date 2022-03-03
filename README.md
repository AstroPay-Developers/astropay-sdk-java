# astropay-sdk-java

## Make a Deposit  

Do a deposit request for the user to pay with its preferred option.

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

> If a user.user_id (user id at Astropay) is specified in the request then the "pay with different account" option will not be available in the checkout.

## optional settings:

### Visual Info  
With this object you can customize how the name will be presented in the AstroPay Cashout page and in the user's wallet. You can also set a logo.

If this object is not set or not included, it will be shown the Business name of your Merchant Account or, if it's not set, blank.

```java
VisualInfo visualInfo = new VisualInfo("MERCHANT NAME", "URL-MERCHANT-LOGO");
```

### User Object 

