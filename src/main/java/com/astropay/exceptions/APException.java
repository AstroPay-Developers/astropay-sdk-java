package com.astropay.exceptions;

/**
 * AstroPay APBase Exception Class
 */
public class APException extends Exception {
    private String depositId;

    public APException(String message) {
        this(message, null, null);
    }

    public APException(String depositId, String message) {
        super(message, null);
        this.depositId = depositId;
    }

    public APException(String depositId, String message, Throwable cause) {
        super(message, cause);
        this.depositId = depositId;
    }

    public APException(Throwable cause) {
        super(cause);
    }

    public String getDepositId() {
        return depositId;
    }

    @Override
    public String toString() {
        String depositId = "";
        if (!this.getDepositId().isEmpty()) {
            depositId = "; merchant_deposit_id: " + this.getDepositId();
        }
        return super.toString() + depositId;
    }
}
