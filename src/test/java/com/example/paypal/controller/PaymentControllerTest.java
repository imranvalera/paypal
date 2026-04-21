package com.example.paypal.controller;

import com.example.paypal.Main;
import com.example.paypal.dto.CaptureRequest;
import com.example.paypal.dto.CreateOrderRequest;
import com.example.paypal.service.PaypalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = PaymentController.class)
@ContextConfiguration(classes = Main.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaypalService paypalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_Returns200() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setAmount(100.0);
        when(paypalService.createOrder(any())).thenReturn(Collections.emptyMap());
        mockMvc.perform(post("/api/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void authorize_Returns200() throws Exception {
        when(paypalService.authorizeOrder(anyString())).thenReturn(Collections.emptyMap());
        mockMvc.perform(post("/api/authorize/ORDER-123"))
                .andExpect(status().isOk());
    }

    @Test
    void capture_Returns200() throws Exception {
        CaptureRequest req = new CaptureRequest();
        req.setAmount(50.0);
        when(paypalService.capture(anyString(), any())).thenReturn(Collections.emptyMap());
        mockMvc.perform(post("/api/capture/AUTH-111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}