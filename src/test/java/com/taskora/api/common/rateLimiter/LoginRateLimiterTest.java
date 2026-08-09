package com.taskora.api.common.ratelimit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.taskora.api.common.exception.RateLimitExceededException;

class LoginRateLimiterTest {

    @Test
    void allowsRequestsUpToMaxAttempts() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, 60);

        assertDoesNotThrow(() -> {
            limiter.checkAllowed("127.0.0.1");
            limiter.checkAllowed("127.0.0.1");
            limiter.checkAllowed("127.0.0.1");
        });
    }

    @Test
    void throwsWhenExceedingMaxAttempts() {
        LoginRateLimiter limiter = new LoginRateLimiter(2, 60);

        limiter.checkAllowed("127.0.0.1");
        limiter.checkAllowed("127.0.0.1");

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> limiter.checkAllowed("127.0.0.1"));

        assertTrue(exception.getRetryAfterSeconds() > 0);
    }

    @Test
    void resetsAfterWindowExpires() throws InterruptedException {
        LoginRateLimiter limiter = new LoginRateLimiter(1, 1);

        limiter.checkAllowed("127.0.0.1");
        assertThrows(RateLimitExceededException.class,
                () -> limiter.checkAllowed("127.0.0.1"));

        Thread.sleep(1100);

        assertDoesNotThrow(() -> limiter.checkAllowed("127.0.0.1"));
    }

    @Test
    void tracksSeparateClientsIndependently() {
        LoginRateLimiter limiter = new LoginRateLimiter(1, 60);

        assertDoesNotThrow(() -> limiter.checkAllowed("127.0.0.1"));
        assertDoesNotThrow(() -> limiter.checkAllowed("192.168.0.1"));
    }
}