package com.example.paymentservice.web.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final Duration ENTRY_TTL = Duration.ofMinutes(30);
    private static final long CLEANUP_EVERY_REQUESTS = 1_000;

    private final ConcurrentHashMap<String, BucketEntry> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requestCount = new AtomicLong();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String clientIp = request.getRemoteAddr();
        BucketEntry bucketEntry = buckets.computeIfAbsent(clientIp, this::newBucketEntry);
        bucketEntry.touch();
        Bucket bucket = bucketEntry.bucket();

        cleanupExpiredBucketsIfNeeded();

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(429);
            response.getWriter().write("Too Many Requests");
        }
    }

    private BucketEntry newBucketEntry(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();

        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();

        return new BucketEntry(bucket);
    }

    private void cleanupExpiredBucketsIfNeeded() {
        if (requestCount.incrementAndGet() % CLEANUP_EVERY_REQUESTS != 0) {
            return;
        }

        Instant now = Instant.now();
        for (Map.Entry<String, BucketEntry> entry : buckets.entrySet()) {
            if (Duration.between(entry.getValue().lastAccessedAt(), now).compareTo(ENTRY_TTL) > 0) {
                buckets.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private static final class BucketEntry {
        private final Bucket bucket;
        private volatile Instant lastAccessedAt;

        private BucketEntry(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessedAt = Instant.now();
        }

        private Bucket bucket() {
            return bucket;
        }

        private Instant lastAccessedAt() {
            return lastAccessedAt;
        }

        private void touch() {
            lastAccessedAt = Instant.now();
        }
    }
}
