# AstroPay SDK for Java

This library provides developers with a simple set of bindings to help you integrate AstroPay API and start processing your payment with Astropay.

## 💡 Requirements

Java 1.7 or higher

## 💻 Installation

1. Append AstroPay dependencies to pom.xml

```xml
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

> **To register:** Create your Astropay Merchant account in [our site](https://merchants-stg.astropay.com/signup).
> **Get Credentials:** After the registration is finished you will have access to your credentials, your account configuration and your Back-Office at: https://merchants-stg.astropay.com/

## Make a Deposit

Do a deposit request for the user to pay with its preferred option.

Simple usage looks like:

```java
import com.astropay.AstroPay;
import com.astropay.exceptions.APException;
import com.astropay.resources.*;

public class Main {
    public static void main(String[] args) {
        AstroPay.Sdk.setSecretKey("YOUR_SECRET_KEY");
        AstroPay.Sdk.setApiKey("YOUR_API_KEY");
        AstroPay.Sdk.registerDepositResultEventListener(this); // your class should implements DepositResultListener
        AstroPay.Sdk.setSandboxMode(true); //optional, default false

        Product product = new Product("merchant_code", "product_merchant_code", "product description");

        User user = new User("exampleuser@example.com");

        Deposit deposit = new Deposit(user, product);
        deposit.setAmount(new BigDecimal("29.77"));
        deposit.setCurrency(Currencies.USD);
        deposit.setCountry(Countries.Uruguay);
        deposit.setMerchantDepositId("merchant_deposit_id");
        deposit.setCallbackUrl("https://your-callback-url");

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
        public void OnDepositStatusResult(DepositResponse depositResponse) {
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

#### Deposit object

```java
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

#### User object

User additional information (optional)

```java
user.setUserId("Astropay_User_ID");
user.setDocument("User_Identification_Document");
user.setDocumentType(DocumentType.CI); //CI, DNI, PASSPORT
user.setEmail("User_email");
user.setPhone("User_phone");
user.setFirstName("User's_first_name");
user.setLastName("User's_last_name");
user.setBirthDate("YYYY-MM-AA"); //Date with format YYYY-MM-DD)
user.setCountry(Countries.Uruguay); //String (2) ISO Code
```

#### Product object

Product additional information (optional)

> Contact your Account Manager or commercial@astropay.com for the MCC Code

```java
product.setCategory("Merchant_category");
```

#### Visual Info

With this object you can customize how the name will be presented in the AstroPay Cashout page and in the user's wallet. You can also set a logo.

If this object is not set or not included, it will be shown the Business name of your Merchant Account or, if it's not set, blank.

```java
VisualInfo visualInfo = new VisualInfo("MERCHANT_NAME", "URL-MERCHANT-LOGO");
```

### Check deposit status

Request made in order to find out the status of a deposit. The response of the request if successful will return the deposit_external_id and the status with three possible values: "PENDING", "APPROVED" or "CANCELLED".

```java
AstroPay.Sdk.checkDepositStatus("deposit_external_id"); //deposit_external_id must be String(128)
```

**OnDepositStatusResult** evet will be emitted.

#### DepositResponse object

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

```java
String deposit_external_id //Astropay Deposit ID
String merchant_deposit_id //Merchant's Deposit ID
String deposit_user_id //Astropay User ID (optional)
String merchant_user_id //Merchant's User ID
String status //Deposit Status
Date end_status_date //Deposit end status date
```

> If we can identify the user who paid the transaction we will send it to the merchant as 'deposit_user_id' for future reference.

## Cashouts

We currently offer two different versions of cashouts, each with their own features and way of implementation, both following almost the same requirements to the parameters.

### Closed Loop Transactions

If you wish to use the user_id parameter in any deposit or cashout request, you will need the AstroPay User ID. This will be included in the callback of a approved Deposit as "deposit_user_id" or a Cashout with "cashout_user_id"

Keep in mind each user in AstroPay has their own unique user id, a user will never share the same ids, you should keep record of them as soon a deposit is completed.

> AstroPay Users do not know their User ID.

### What is Closed Loop?

Closed Loop means a user in the merchant's end will only be able to request a Cashout if they have deposited previously using AstroPay.

It is also possible to link a AstroPay account to a Merchant Account so only the specified one complete deposits with the parameter user_id in the request.

In order to do that, check if your user while trying to cashout has their "deposit_user_id" saved and use that to request the cashout with the user_id parameter.

**We highly encourage the user is made aware of this condition to cashout.**

> Notice if both user_id and phone number are informed, the priority is to the user_id.

### Cashout V1

As soon the request is received by Astropay, it gets validated to the corresponding account.

```java
public class Main {
    public static void main(String[] args) {
        AstroPay.Sdk.setSecretKey("YOUR_SECRET_KEY");
        AstroPay.Sdk.setApiKey("YOUR_API_KEY");
        AstroPay.Sdk.registerCashoutV1ResultEventListener(this); // your class should implements CashoutV1ResultListener
        AstroPay.Sdk.setSandboxMode(true); //optional, default false

        User user = new User("exampleuser@example.com"); //This parameter is meant to be a unique identifier of the user on the merchant’s side. It is very important to send the correct ID on your side in order for Astropay to run several fraud and risk controls.
        user.setPhone("598XXXXXXXX"); // Either user_id or phone must be specified.
        user.setUserId("user_id"); // for “closed-loop”

        CashoutV1 cashoutV1 = new CashoutV1(user);
        cashoutV1.setAmount(new BigDecimal("5.99"));
        cashoutV1.setCurrency(Currencies.USD);
        cashoutV1.setCountry(Countries.Uruguay);
        cashoutV1.setMerchantCashoutId("merchant_cashout_id");
        cashoutV1.setCallbackUrl("https://your-callback-url");

        cashoutV1.init();

        //result listeners
        @Override
        public void OnCashoutSuccess(CashoutV1Response cvr) {
            System.out.println(cvr.getCashoutId() + ", " + cvr.getStatus());
        }

        @Override
        public void OnCashoutError(CashoutV1Response cvr) {
            System.out.println(cvr.getError() + ", " + cvr.getDescription());
        }

        @Override
        public void OnCashoutStatusResult(CashoutV1Response cvr) {
            System.out.println(cvr.getCashoutId() + ", " + cvr.getStatus());
        }
    }
}
```

If the cashouts is created as PENDING, it means we could not find an account with the information provided. A SMS will be sent to the phone number so the user create an account and receives his amount.

> Cashouts will only stay pending for 24 hours, after that they will be cancelled and a callback sent to the specified URL

### Callbacks

A callback will be sent by POST protocol to the callback URL provided in the request. Callbacks are only sent when the cashout changes status from PENDING to APPROVED or CANCELLED.

```java
String cashout_id	// Astropay Cashout Id
String merchant_cashout_id	// Merchant's cashout Id
String cashout_user_id // Astropay User Id (optional)
String merchant_user_id	// Merchant's user Id
String status // Cashout status
Date end_status_date //Deposit End status date
```

> Only 'APPROVED' cashouts will include the cashout_user_id.

### Checking Cashout Status

If necessary, you can manually check a cashout status.

> cashout_id must be an integer

```java
AstroPay.Sdk.checkCashoutV1Status(cashout_id);
```

**OnCashoutStatusResult** evet will be emitted.

#### Cashout object

```java
BigDecimal amount;
String currency;
String country;
String merchantCashoutId; //Unique identifier of merchant transaction
URL callbackUrl; //A URL where the merchant will be receiving transaction status updates. See callback for more details
User user; //User Object
```

#### User object

User additional information (optional)

```java
user.setUserId("Astropay_User_ID");
user.setDocument("User_Identification_Document");
user.setDocumentType(DocumentType.CI); //CI, DNI, PASSPORT
user.setEmail("User_email");
user.setPhone("User_phone");
user.setFirstName("User's_first_name");
user.setLastName("User's_last_name");
user.setBirthDate("YYYY-MM-AA"); //Date with format YYYY-MM-DD)
user.setCountry(Countries.Uruguay); //String (2) ISO Code

## Cashout V2

The user must be redirected similar to the Deposit flow, authenticate and complete the transaction. This version of cashouts also provides additional security features such as On Hold cashouts

```java
public class Main {
    public static void main(String[] args) {
        AstroPay.Sdk.setSecretKey("YOUR_SECRET_KEY");
        AstroPay.Sdk.setApiKey("YOUR_API_KEY");
        AstroPay.Sdk.registerCashoutV2ResultEventListener(this); // your class should implements CashoutV2ResultListener
        AstroPay.Sdk.setSandboxMode(true); //optional, default false

        User user = new User("exampleuser@example.com");
        user.setPhone("598XXXXXXXX"); // Either user_id or phone must be specified.
        user.setUserId("user_id"); // for “closed-loop”

        CashoutV2 cashoutV2 = new CashoutV2(user);
        cashoutV2.setAmount(new BigDecimal("2.99"));
        cashoutV2.setCurrency(Currencies.USD);
        cashoutV2.setCountry(Countries.Uruguay);
        cashoutV2.setMerchantCashoutId("merchant_cashout_id");
        cashoutV2.setCallbackUrl("https://your-callback-url");

        VisualInfo visualInfo = new VisualInfo("MERCHANT_NAME", "URL-MERCHANT-LOGO");
        cashoutV2.setVisualInfo(visualInfo);

        cashoutV2.init();

        //Result listeners
        @Override
        public void OnCashoutSuccess(CashoutV2Response cashoutV2Response) {
            System.out.println(cashoutV2Response.getCashoutExternalId() + ", " + cashoutV2Response.getStatus());
        }

        @Override
        public void OnCashoutError(CashoutV2Response cashoutV2Response) {
            System.out.println(cashoutV2Response.getError() + ", " + cashoutV2Response.getDescription());
        }

        @Override
        public void OnCashoutStatusResult(CashoutV2Response cashoutV2Response) {
            System.out.println(cashoutV2Response.getCashoutExternalId() + ", " + cashoutV2Response.getStatus());
        }
    }
}
```

### Response

Since the v2 provides a more interactive experience for the user with AstroPay, you must redirect the user to the provided OneTouch Link in the response.

```java
String status;
String url;
String merchant_cashout_id;
String cashout_external_id;
```

### Error message

Sometimes a request won't get into one of the three status in the previous point, if the request cannot be completed, you will receive an error message. Here's an example of one.

```java
String error; //Error Code
String description; //Error Description
```

> Note the error messages in this page assumes the request is made correctly. If you receive an error like UNAUTHORIZED, see Error Codes.

### Cashouts On Hold

Cashouts On Hold is a Security feature that allows the merchant to analyze who's trying to cashout and decides if it should be approved or not. After the flow of cashout v2 when the customer fills his AstroPay account information and finishes the process, a notification will be sent to the merchant's on_hold_confirmation_url. AstroPay will be expecting within 24h a response in the On Hold endpoint to either approve or cancel the user request.

#### How to create a On Hold Cashout?

Add the following parameters to your cashout

```java
CashoutOnHold cashoutOnHold = new CashoutOnHold();
cashoutOnHold.createOnHold = true; //Enable On Hold for the request
cashoutOnHold.onHoldConfirmationUrl = "https://on-hold-confirm-url";
cashoutV2.setSecurity(cashoutOnHold); //On Hold notification URL
```

> on_hold_confirmation_url is required if create_on_hold is set to true.

#### How to approve or cancel a cashout that is on hold?

```java
int status = AstroPay.Sdk.confirmCashoutOnHold("cashout_external_id", true); //Merchant’s cashout approval/denial (true/false)
JOptionPane.showMessageDialog(this, "Confirm status result: " + status);
```

> Keep in mind a notification to approve or cancel the cashout will only be sent to the merchant's on_hold_confirmation_url after the user is redirected to AstroPay and fills his information.

> After the merchant approves or cancel a cashout, a notification with the final status will be sent to the merchant's callback URL.

### Checking Cashout Status

If necessary, you can manually check a cashout status. Please note this is not required as a callback with the final status will be sent within 24h.

```java
AstroPay.Sdk.checkCashoutV2Status("cashout_external_id");
```

**OnCashoutStatusResult** evet will be emitted.

> cashout_external_id must be a String

## ❤️ Support

If you require technical support, please contact [support](https://astropay.com/contact/?lang=en)

If you want, you can check our [AstroPay OneTouch API Guide](https://developers-wallet.astropay.com/?php#getting-started-with-one-touch) for more information.

## 🏻 License

Copyright © 2020 ASTROPAY. All rights reserved.
