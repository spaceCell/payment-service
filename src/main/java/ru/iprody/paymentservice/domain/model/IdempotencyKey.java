package ru.iprody.paymentservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
@NoArgsConstructor
@EqualsAndHashCode(of = "key")
@Getter
@Setter
public class IdempotencyKey {

    @Id
    @Column(name = "key_value")
    private String key;

    @Enumerated(EnumType.STRING)
    private IdempotencyKeyStatus status;

    @Lob
    private String response;

    private int statusCode;

    private LocalDateTime createdAt;

    public IdempotencyKey(String key, IdempotencyKeyStatus status) {
        this.key = key;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
