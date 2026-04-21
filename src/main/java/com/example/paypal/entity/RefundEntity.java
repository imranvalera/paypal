package com.example.paypal.entity;

import jakarta.persistence.*;

@Entity
public class RefundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String refundId;
    private Double amount;

    @ManyToOne
    private CaptureEntity capture;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getRefundId() {
        return refundId;
    }
    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public CaptureEntity getCapture() {
        return capture;
    }
    public void setCapture(CaptureEntity capture) {
        this.capture = capture;
    }
}