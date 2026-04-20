package com.example.paymentservice.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.paymentservice.common.IdempotencyKeyExistsException;
import com.example.paymentservice.domain.model.IdempotencyKey;
import com.example.paymentservice.domain.model.IdempotencyKeyStatus;
import com.example.paymentservice.domain.repository.IdempotencyRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    @Override
    @Transactional
    public void createPendingKey(String key) {
        try {
            idempotencyRepository.save(new IdempotencyKey(key, IdempotencyKeyStatus.PENDING));
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyKeyExistsException("Key already exists", e);
        }
    }

    @Override
    public Optional<IdempotencyKey> getByKey(String key) {
        return idempotencyRepository.findById(key);
    }

    @Override
    @Transactional
    public void markAsCompleted(String key, String response, int statusCode) {
        IdempotencyKey idempotencyKey = idempotencyRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException("Idempotency key not found: " + key));
        idempotencyKey.setStatus(IdempotencyKeyStatus.COMPLETED);
        idempotencyKey.setResponse(response);
        idempotencyKey.setStatusCode(statusCode);
        idempotencyRepository.save(idempotencyKey);
    }
}
