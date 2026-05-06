package ru.iprody.paymentservice.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;
import ru.iprody.paymentservice.application.IdempotencyService;
import ru.iprody.paymentservice.common.IdempotencyKeyExistsException;
import ru.iprody.paymentservice.domain.model.IdempotencyKey;
import ru.iprody.paymentservice.domain.model.IdempotencyKeyStatus;
import ru.iprody.paymentservice.web.filter.ResponseCachingFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";

    private final IdempotencyService idempotencyService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        var method = HttpMethod.valueOf(request.getMethod());
        if (!method.equals(HttpMethod.POST) && !method.equals(HttpMethod.PATCH)) {
            return true;
        }

        String key = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (key == null || key.isBlank()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("X-Idempotency-Key header is required");
            return false;
        }

        Optional<IdempotencyKey> existing = idempotencyService.getByKey(key);
        if (existing.isPresent()) {
            return handleExistingKey(existing.get(), response);
        }

        try {
            idempotencyService.createPendingKey(key);
            return true;
        } catch (IdempotencyKeyExistsException e) {
            response.setStatus(HttpStatus.CONFLICT.value());
            response.getWriter().write("Same request is already in progress");
            return false;
        }
    }

    private boolean handleExistingKey(IdempotencyKey idempotencyKey,
                                      HttpServletResponse response) throws IOException {
        if (idempotencyKey.getStatus() == IdempotencyKeyStatus.PENDING) {
            response.setStatus(HttpStatus.CONFLICT.value());
            response.getWriter().write("Same request is already in progress");
            return false;
        }
        if (idempotencyKey.getStatus() == IdempotencyKeyStatus.COMPLETED) {
            response.setStatus(idempotencyKey.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(idempotencyKey.getResponse());
            return false;
        }
        throw new IllegalArgumentException("Unknown idempotency key status: " + idempotencyKey.getStatus());
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) throws Exception {
        var method = HttpMethod.valueOf(request.getMethod());
        if (!method.equals(HttpMethod.POST) && !method.equals(HttpMethod.PATCH)) {
            return;
        }

        String key = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (key == null || key.isBlank()) {
            return;
        }

        var wrappedResponse = (ContentCachingResponseWrapper) request.getAttribute(
                ResponseCachingFilter.WRAPPED_RESPONSE_ATTRIBUTE);
        if (wrappedResponse == null) {
            return;
        }

        String responseBody = new String(wrappedResponse.getContentAsByteArray(),
                wrappedResponse.getCharacterEncoding());
        idempotencyService.markAsCompleted(key, responseBody, response.getStatus());
    }
}
