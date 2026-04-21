package com.example.paypal.service;

import com.example.paypal.dto.CreateOrderRequest;
import com.example.paypal.dto.CaptureRequest;
import com.example.paypal.dto.RefundRequest;
import com.example.paypal.entity.*;
import com.example.paypal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PaypalService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.secret}")
    private String secret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired private OrderRepository orderRepo;
    @Autowired private AuthorizationRepository authRepo;
    @Autowired private CaptureRepository captureRepo;
    @Autowired private RefundRepository refundRepo;

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, secret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/oauth2/token",
                HttpMethod.POST,
                request,
                Map.class
        );
        return (String) response.getBody().get("access_token");
    }

    public Map createOrder(CreateOrderRequest req) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);





        Map<String, Object> body = new HashMap<>();
        body.put("intent", "AUTHORIZE");
        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("invoice_id", req.getInvoiceId());
        Map<String, String> amount = new HashMap<>();
        amount.put("currency_code", "GBP");
        amount.put("value", String.valueOf(req.getAmount()));
        purchaseUnit.put("amount", amount);
        body.put("purchase_units", List.of(purchaseUnit));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        Map<String, Object> applicationContext = new HashMap<>();
        applicationContext.put("return_url", "https://www.sandbox.paypal.com/checkoutnow/error"); //test for errors. check if working later.
        applicationContext.put("cancel_url", "https://www.sandbox.paypal.com/checkoutnow/error");
        body.put("application_context", applicationContext);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                baseUrl + "/v2/checkout/orders",
                HttpMethod.POST,
                request,
                Map.class
        );
        Map responseBody = responseEntity.getBody();
        OrderEntity entity = new OrderEntity();
        entity.setPaypalOrderId((String) responseBody.get("id"));
        entity.setInvoiceId(req.getInvoiceId());
        entity.setTotalAmount(req.getAmount());
        entity.setStatus("CREATED");
        orderRepo.save(entity);
        return responseBody;
    }

    public Map authorizeOrder(String orderId) {
        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                baseUrl + "/v2/checkout/orders/" + orderId + "/authorize",
                HttpMethod.POST,
                request,
                Map.class
        );
        Map responseBody = responseEntity.getBody();
        OrderEntity order = orderRepo.findByPaypalOrderId(orderId);
        if (order != null) {
            try {
                List<Map> purchaseUnits = (List) responseBody.get("purchase_units");
                Map payments = (Map) purchaseUnits.get(0).get("payments");
                List<Map> authorizations = (List) payments.get("authorizations");
                String authId = (String) authorizations.get(0).get("id");
                AuthorizationEntity authEntity = new AuthorizationEntity();
                authEntity.setAuthorizationId(authId);
                authEntity.setAmount(order.getTotalAmount());
                authEntity.setRemainingAmount(order.getTotalAmount());
                authEntity.setStatus("AUTHORIZED");
                authEntity.setOrder(order);
                authRepo.save(authEntity);
                order.setStatus("AUTHORIZED");
                orderRepo.save(order);
            } catch (Exception e) {
                System.out.println("Could not parse authorization ID for DB: " + e.getMessage());
            }
        }

        return responseBody;
    }

    public Map capture(String authorizationId, CaptureRequest req) {
        AuthorizationEntity auth = authRepo.findAll().stream()
                .filter(a -> a.getAuthorizationId().equals(authorizationId))
                .findFirst().orElse(null);

        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        Map<String, String> amount = new HashMap<>();
        amount.put("currency_code", "GBP");
        amount.put("value", String.valueOf(req.getAmount()));
        body.put("amount", amount);
        body.put("final_capture", req.isFinalCapture());
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                baseUrl + "/v2/payments/authorizations/" + authorizationId + "/capture",
                HttpMethod.POST,
                request,
                Map.class
        );

        Map responseBody = responseEntity.getBody();

        if (auth != null && responseBody.get("id") != null) {
            CaptureEntity capture = new CaptureEntity();
            capture.setCaptureId((String) responseBody.get("id"));
            capture.setAmount(req.getAmount());
            capture.setFinalCapture(req.isFinalCapture());
            capture.setAuthorization(auth);
            captureRepo.save(capture);
            auth.setRemainingAmount(auth.getRemainingAmount() - req.getAmount());
            if (req.isFinalCapture()) auth.setStatus("CAPTURED_FINAL");
            authRepo.save(auth);
        }

        return responseBody;
    }

    public Map refund(String captureId, RefundRequest req) {
        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        Map<String, String> amount = new HashMap<>();
        amount.put("value", String.valueOf(req.getAmount()));
        amount.put("currency_code", "GBP");
        body.put("amount", amount);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                baseUrl + "/v2/payments/captures/" + captureId + "/refund",
                HttpMethod.POST,
                request,
                Map.class
        );

        Map responseBody = responseEntity.getBody();

        if (responseBody != null && responseBody.get("id") != null) {
            CaptureEntity capture = captureRepo.findAll().stream()
                    .filter(c -> c.getCaptureId().equals(captureId))
                    .findFirst().orElse(null);
            RefundEntity refundEntity = new RefundEntity();
            refundEntity.setRefundId((String) responseBody.get("id"));
            refundEntity.setAmount(req.getAmount());
            refundEntity.setCapture(capture);
            refundRepo.save(refundEntity);
        }

        return responseBody;
    }
}