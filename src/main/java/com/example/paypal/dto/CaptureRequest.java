package com.example.paypal.dto;

public class CaptureRequest {

    private Double amount;
    private boolean finalCapture;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public boolean isFinalCapture() {
        return finalCapture;
    }

    public void setFinalCapture(boolean finalCapture) {
        this.finalCapture = finalCapture;
    }
}