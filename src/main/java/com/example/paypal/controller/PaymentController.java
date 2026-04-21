package com.example.paypal.controller;

import com.example.paypal.dto.CaptureRequest;
import com.example.paypal.dto.CreateOrderRequest;
import com.example.paypal.dto.RefundRequest;
import com.example.paypal.service.PaypalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaypalService payPalService;

    @PostMapping("/create-order")
    public Map createOrder(@RequestBody CreateOrderRequest request) {
        return payPalService.createOrder(request);
    }

    @PostMapping("/authorize/{orderId}")
    public Map authorize(@PathVariable String orderId) {

        return payPalService.authorizeOrder(orderId);
    }

    @PostMapping("/capture/{authId}")
    public Map capture(@PathVariable String authId,
                       @RequestBody CaptureRequest request) {
        return payPalService.capture(authId, request);
    }

    @PostMapping("/refund/{captureId}")
    public Map refund(@PathVariable String captureId,
                      @RequestBody RefundRequest request) {
        return payPalService.refund(captureId, request);
    }
}