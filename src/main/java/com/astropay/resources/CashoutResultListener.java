package com.astropay.resources;

public interface CashoutResultListener {
    void OnCashoutSuccess(CashoutResponse cashoutResponse);

    void OnCashoutError(CashoutResponse cashoutResponse);

    void OnStatusResult(CashoutResponse cashoutResponse);
}
