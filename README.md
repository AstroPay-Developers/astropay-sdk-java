# AstroPay SDK for Java
This library provides developers with a simple set of bindings to help you integrate AstroPay API and start processing your payment with Astropay.

## 💡 Requirements

Java 1.7 or higher

## 📲 Installation

1. Append AstroPay dependencies to pom.xml

``` xml
...
  <dependencies>
      <dependency>
          <groupId>org.astropay</groupId>
          <artifactId>AstropaySDK</artifactId>
          <version>1.0.0</version>
      </dependency>
  </dependencies>
  ...

```
2. Run <code>mvn install</code> and thats all, you have AstroPay SDK installed.

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

        Deposit deposit = new Deposit(user, product);
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

## 📚 Documentation

> **To register:** Create your Astropay Merchant account in [our site](https://merchants-stg.astropay.com/signup). 
> **Get Credentials:** After the registration is finished you will have access to your credentials, your account configuration and your Back-Office at: https://merchants-stg.astropay.com/

### Deposit Object

``` java
BigDecimal amount;
String currency;
String country;
String merchantDepositId; //Unique identifier of transaction
String depositExternalId;
URL callbackUrl;
URL redirectUrl; //URL to redirect the user after the deposit flow (optional)
VisualInfo visualInfo; //Visual Info object (optional)
Product product;
User user;
```
> To be provided if the notification URL is different from the notification URL registered by the merchant. A notification will be sent at every change in deposit's status to the merchant notification URL by POST protocol. See callback for more details.

### User Object

User additional information (optional)

``` java
user.setUserId("Astropay_User_ID");
user.setDocument("User_Identification_Document");
user.setDocumentType(DocumentType.CI); //CI, DNI, PASSPORT
user.setEmail("User_email");
user.setPhone("User_phone");
user.setFirstName("User's_first_name");
user.setLastName("User's_last_name");
user.setBirthDate(DateFormat.parse("YYY-MM-AA")); //Date with format YYYY-MM-DD)
user.setCountry(Countries.Uruguay); //String (2) ISO Code
```

### Product Object

Product additional information (optional)

> Contact your Account Manager or commercial@astropay.com for the MCC Code

``` java
product.setCategory("Merchant_category");
```

### Visual Info  
With this object you can customize how the name will be presented in the AstroPay Cashout page and in the user's wallet. You can also set a logo.

If this object is not set or not included, it will be shown the Business name of your Merchant Account or, if it's not set, blank.

```java
VisualInfo visualInfo = new VisualInfo("MERCHANT_NAME", "URL-MERCHANT-LOGO");
```

### Check deposit status

Request made in order to find out the status of a deposit. The response of the request if successful will return the deposit_external_id and the status with three possible values: "PENDING", "APPROVED" or "CANCELLED".

``` java
AstroPay.Sdk.checkDepositStatus("deposit_external_id"); //deposit_external_id must be String(128)
```

### DepositResponse Object

You must implement <code>DepositResultListener</code>

```java
String status; //{'PENDING','APPROVED','CANCELLED'}
String url; //URL to redirect the user
String merchant_deposit_id; //Merchant deposit Id
String deposit_external_id; //Astropay deposit Id	
String error; //Error Code
String description; //Error Description
```
### Callback

A callback is sent whenever the transaction status changes to APPROVED or CANCELLED

``` java
String deposit_external_id //Astropay Deposit ID
String merchant_deposit_id //Merchant's Deposit ID	
String deposit_user_id //Astropay User ID (optional)
String merchant_user_id //Merchant's User ID	
String status //Deposit Status	
Date end_status_date //Deposit end status date
```
> If we can identify the user who paid the transaction we will send it to the merchant as 'deposit_user_id' for future reference.


