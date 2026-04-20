package com.example.paymentservice.application;

import com.example.paymentservice.domain.model.IdempotencyKey;

import java.util.Optional;

public interface IdempotencyService {

    void createPendingKey(String key);

    Optional<IdempotencyKey> getByKey(String key);

    void markAsCompleted(String key, String response, int statusCode);
}
