package com.example.paypal.repository;

import com.example.paypal.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    OrderEntity findByPaypalOrderId(String paypalOrderId);
}