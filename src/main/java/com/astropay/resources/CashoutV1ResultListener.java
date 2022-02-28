package com.astropay.resources;

public interface CashoutV1ResultListener {
    void OnCashoutSuccess(CashoutV1Response cashoutV1Response);
    void OnCashoutError(CashoutV1Response cashoutV1Response);
    void OnCashoutStatusResult(CashoutV1Response cashoutV1Response);
}
