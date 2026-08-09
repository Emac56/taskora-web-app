package com.taskora.api.common.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.taskora.api.common.exception.RateLimitExceededException;

/**
 * In-memory, fixed-window rate limiter for the login endpoint.
 *
 * Limitation: counters are held in local memory only. If Taskora is
 * ever deployed across multiple application instances, this must be
 * replaced with a shared store (e.g. Redis) so limits are enforced
 * consistently across instances.
 */
@Component
public class LoginRateLimiter {

    private final int maxAttempts;
    private final long windowMillis;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public LoginRateLimiter(
            @Value("${app.rate-limit.login.max-attempts}") int maxAttempts,
            @Value("${app.rate-limit.login.window-seconds}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowSeconds * 1000L;
    }

    public void checkAllowed(String clientId) {
        Bucket bucket = buckets.computeIfAbsent(clientId, key -> new Bucket());

        synchronized (bucket) {
            long now = System.currentTimeMillis();

            if (now - bucket.windowStart >= windowMillis) {
                bucket.windowStart = now;
                bucket.count = 0;
            }

            bucket.count++;

            if (bucket.count > maxAttempts) {
                long remainingMillis = windowMillis - (now - bucket.windowStart);
                long retryAfterSeconds = Math.max(1, remainingMillis / 1000);

                throw new RateLimitExceededException(
                        "Too many login attempts. Please try again later.",
                        retryAfterSeconds);
            }
        }
    }

    private static final class Bucket {
        private long windowStart = System.currentTimeMillis();
        private int count = 0;
    }
}