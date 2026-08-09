package com.taskora.api.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.taskora.api.common.dto.response.ApiErrorResponse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleResourceNotFoundException() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Tutorial not found.");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleResourceNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals(
                "Tutorial not found.",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleInvalidCredentialsException() {

        IllegalArgumentException exception =
                new IllegalArgumentException("Invalid username or password.");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleInvalidCredentials(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals(
                "Invalid username or password.",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleRateLimitExceededException() {

        RateLimitExceededException exception =
                new RateLimitExceededException("Too many login attempts.", 30L);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleRateLimitExceeded(exception);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals(
                "Too many login attempts.",
                response.getBody().getMessage()
        );
        assertEquals(
                "30",
                response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)
        );
    }

    @Test
    void shouldNotSetRetryAfterHeaderWhenNotRateLimited() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Tutorial not found.");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleResourceNotFound(exception);

        assertNull(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
    }
}