package com.taskora.api.common.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import org.springframework.beans.factory.annotation.Autowired;
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
    private final LongSupplier clock;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanup;

    /**
     * Primary constructor for Spring dependency injection.
     * Values are injected from application configuration.
     */
    @Autowired
    public LoginRateLimiter(
            @Value("${app.rate-limit.login.max-attempts}") int maxAttempts,
            @Value("${app.rate-limit.login.window-seconds}") long windowSeconds) {
        this(maxAttempts, windowSeconds, System::currentTimeMillis);
    }

    /**
     * Visible for testing: allows tests to control the passage of time
     * without relying on Thread.sleep, keeping window-expiry tests
     * fast and deterministic.
     */
    LoginRateLimiter(int maxAttempts, long windowSeconds, LongSupplier clock) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowSeconds * 1000L;
        this.clock = clock;
        this.lastCleanup = new AtomicLong(clock.getAsLong());
    }

    public void checkAllowed(String clientId) {
        maybeCleanupExpiredBuckets();

        Bucket bucket = buckets.computeIfAbsent(
                clientId, key -> new Bucket(clock.getAsLong()));

        synchronized (bucket) {
            long now = clock.getAsLong();

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

    /**
     * Sweeps buckets whose window has already expired, so memory does not
     * grow without bound over the life of the process. Runs at most once
     * per window length, triggered opportunistically by incoming requests
     * rather than a background scheduler.
     */
    private void maybeCleanupExpiredBuckets() {
        long now = clock.getAsLong();
        long last = lastCleanup.get();

        if (now - last < windowMillis) {
            return;
        }
        if (!lastCleanup.compareAndSet(last, now)) {
            return;
        }

        buckets.entrySet().removeIf(entry -> {
            Bucket bucket = entry.getValue();
            synchronized (bucket) {
                return now - bucket.windowStart >= windowMillis;
            }
        });
    }

    /**
     * Visible for testing: exposes current bucket count so tests can verify
     * that stale entries are evicted instead of accumulating forever.
     */
    int bucketCount() {
        return buckets.size();
    }

    private static final class Bucket {
        private long windowStart;
        private int count = 0;

        private Bucket(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}