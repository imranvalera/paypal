package com.example.paypal.entity;

import jakarta.persistence.*;

@Entity
public class CaptureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String captureId;
    private Double amount;
    private Boolean finalCapture;

    @ManyToOne
    private AuthorizationEntity authorization;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getCaptureId() {
        return captureId;
    }
    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getFinalCapture() {
        return finalCapture;
    }
    public void setFinalCapture(Boolean finalCapture) {
        this.finalCapture = finalCapture;
    }

    public AuthorizationEntity getAuthorization() {
        return authorization;
    }
    public void setAuthorization(AuthorizationEntity authorization) {
        this.authorization = authorization;
    }
}