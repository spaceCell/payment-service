package ru.iprody.paymentservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.iprody.paymentservice.domain.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
