package com.example.paypal.repository;

import com.example.paypal.entity.AuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizationRepository extends JpaRepository<AuthorizationEntity, Long> {
    AuthorizationEntity findByAuthorizationId(String authorizationId);
}