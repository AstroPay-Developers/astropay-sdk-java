package com.astropay.resources;

public interface DepositResultListener {
    void OnDepositSuccess(DepositResponse depositResponse);
    void OnDepositError(DepositResponse depositResponse);
    void OnDepositStatusResult(DepositResponse depositResponse);
}
