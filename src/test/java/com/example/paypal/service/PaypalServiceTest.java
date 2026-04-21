package com.example.paypal.service;

import com.example.paypal.dto.CreateOrderRequest;
import com.example.paypal.entity.OrderEntity;
import com.example.paypal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaypalServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private OrderRepository orderRepo;
    @Mock private AuthorizationRepository authRepo;
    @Mock private CaptureRepository captureRepo;
    @Mock private RefundRepository refundRepo;

    @InjectMocks
    private PaypalService paypalService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paypalService, "clientId", "cid");
        ReflectionTestUtils.setField(paypalService, "secret", "sec");
        ReflectionTestUtils.setField(paypalService, "baseUrl", "http://api");
        ReflectionTestUtils.setField(paypalService, "restTemplate", restTemplate);
    }

    @Test
    void createOrder_ShouldWork() {
        mockToken();
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", "OP-123");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(resp));
        CreateOrderRequest req = new CreateOrderRequest();
        req.setAmount(10.0);
        req.setInvoiceId("INV-1");
        Map result = paypalService.createOrder(req);
        assertNotNull(result);
        verify(orderRepo).save(any());
    }

    @Test
    void authorizeOrder_ShouldWork() {
        mockToken();
        OrderEntity order = new OrderEntity();
        order.setTotalAmount(50.0);
        when(orderRepo.findByPaypalOrderId("OP-123")).thenReturn(order);
        Map<String, Object> auth = Map.of("id", "AUTH-999");
        Map<String, Object> payments = Map.of("authorizations", List.of(auth));
        Map<String, Object> unit = Map.of("payments", payments);
        Map<String, Object> resp = Map.of("purchase_units", List.of(unit));

        when(restTemplate.exchange(contains("/authorize"), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(resp));
        Map result = paypalService.authorizeOrder("OP-123");
        assertNotNull(result);
        verify(authRepo).save(any());
    }

    private void mockToken() {
        Map<String, String> token = Map.of("access_token", "test-token");
        lenient().when(restTemplate.exchange(
                contains("/oauth2/token"),
                eq(HttpMethod.POST),
                any(),
                eq(Map.class))
        ).thenReturn(ResponseEntity.ok(token));
    }
}