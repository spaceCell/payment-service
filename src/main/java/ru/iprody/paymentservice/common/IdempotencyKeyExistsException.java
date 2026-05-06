package ru.iprody.paymentservice.common;

public class IdempotencyKeyExistsException extends RuntimeException {

    public IdempotencyKeyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
