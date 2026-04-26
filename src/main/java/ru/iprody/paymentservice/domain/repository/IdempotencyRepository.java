package ru.iprody.paymentservice.domain.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.lang.NonNull;
import ru.iprody.paymentservice.domain.model.IdempotencyKey;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @NonNull
    Optional<IdempotencyKey> findById(@NonNull String key);
}
