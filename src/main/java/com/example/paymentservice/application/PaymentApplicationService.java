package com.example.paymentservice.application;

import java.util.List;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.paymentservice.application.command.PaymentCommand;
import com.example.paymentservice.application.dto.PaymentAmountDetails;
import com.example.paymentservice.application.dto.PaymentDetails;
import com.example.paymentservice.common.ResourceNotFoundException;
import com.example.paymentservice.domain.model.Payment;
import com.example.paymentservice.domain.model.PaymentAmount;
import com.example.paymentservice.domain.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;

    @Transactional
    @CircuitBreaker(name = "paymentServiceCircuitBreaker")
    public PaymentDetails create(PaymentCommand paymentCommand) {
        Payment payment = new Payment(
                paymentCommand.orderId(),
                paymentCommand.status(),
                paymentCommand.method(),
                toPaymentAmount(paymentCommand.amount())
        );
        return toPaymentDetails(paymentRepository.save(payment));
    }

    @CircuitBreaker(name = "paymentServiceCircuitBreaker")
    public List<PaymentDetails> getAll() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toPaymentDetails)
                .toList();
    }

    @CircuitBreaker(name = "paymentServiceCircuitBreaker")
    public PaymentDetails getById(Long paymentId) {
        return toPaymentDetails(getPayment(paymentId));
    }

    @Transactional
    @CircuitBreaker(name = "paymentServiceCircuitBreaker")
    public PaymentDetails update(Long paymentId, PaymentCommand paymentCommand) {
        Payment payment = getPayment(paymentId);
        payment.update(
                paymentCommand.orderId(),
                paymentCommand.status(),
                paymentCommand.method(),
                toPaymentAmount(paymentCommand.amount())
        );
        return toPaymentDetails(payment);
    }

    @Transactional
    @CircuitBreaker(name = "paymentServiceCircuitBreaker")
    public void delete(Long paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new ResourceNotFoundException("Payment with id " + paymentId + " was not found");
        }
        paymentRepository.deleteById(paymentId);
    }

    private Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + paymentId + " was not found"));
    }

    private PaymentAmount toPaymentAmount(PaymentAmountDetails paymentAmountDetails) {
        if (paymentAmountDetails == null) {
            throw new IllegalArgumentException("Payment amount must be provided");
        }
        return new PaymentAmount(paymentAmountDetails.amount(), paymentAmountDetails.currency());
    }

    private PaymentDetails toPaymentDetails(Payment payment) {
        return new PaymentDetails(
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getMethod(),
                new PaymentAmountDetails(payment.getAmount().getAmount(), payment.getAmount().getCurrency()),
                payment.getCreatedAt()
        );
    }
}
