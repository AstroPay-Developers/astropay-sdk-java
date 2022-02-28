package com.astropay.resources;

public interface CashoutV2ResultListener {
    void OnCashoutSuccess(CashoutV2Response cashoutV2Response);

    void OnCashoutError(CashoutV2Response cashoutV2Response);

    void OnCashoutStatusResult(CashoutV2Response cashoutV2Response);
}
