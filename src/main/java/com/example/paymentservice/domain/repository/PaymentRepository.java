package com.example.paymentservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.paymentservice.domain.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
