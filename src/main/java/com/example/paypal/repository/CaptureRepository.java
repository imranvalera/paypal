package com.example.paypal.repository;

import com.example.paypal.entity.CaptureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaptureRepository extends JpaRepository<CaptureEntity, Long> {
    CaptureEntity findByCaptureId(String captureId);
}