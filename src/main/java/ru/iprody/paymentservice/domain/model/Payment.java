package ru.iprody.paymentservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentMethod method;

    @Embedded
    private PaymentAmount amount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Payment(Long orderId, PaymentStatus status, PaymentMethod method, PaymentAmount amount) {
        changeOrderId(orderId);
        changeStatus(status);
        changeMethod(method);
        changeAmount(amount);
    }

    public void update(Long orderId, PaymentStatus status, PaymentMethod method, PaymentAmount amount) {
        changeOrderId(orderId);
        changeStatus(status);
        changeMethod(method);
        changeAmount(amount);
    }

    public void changeOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order id must be greater than zero");
        }
        this.orderId = orderId;
    }

    public void changeStatus(PaymentStatus status) {
        this.status = status == null ? PaymentStatus.PENDING : status;
    }

    public void changeMethod(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method must be provided");
        }
        this.method = method;
    }

    public void changeAmount(PaymentAmount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount must be provided");
        }
        this.amount = amount;
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }
}
