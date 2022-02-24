package com.astropay.resources;

public interface CashoutV2ResultListener {
    void OnCashoutSuccess(CashoutV2Response cashoutResponse);

    void OnCashoutError(CashoutV2Response cashoutResponse);
}
