package com.astropay.wallet;

public interface DepositResultListener {
    void OnDepositSuccess(DepositResponse depositResponse);
    void OnDepositError(DepositResponse depositResponse);
}
