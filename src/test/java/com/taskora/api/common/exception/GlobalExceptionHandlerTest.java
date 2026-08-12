package com.taskora.api.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
    
    @Test
    void shouldHandleMethodArgumentNotValidException() {

        FieldError fieldError = new FieldError(
                "loginRequest", "email", "Invalid email format.");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals(
                "email: Invalid email format.",
                response.getBody().getMessage()
        );
    }

    @Test
    void shouldHandleGenericException() {

        Exception exception = new RuntimeException("Something broke.");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleGeneric(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals(
                "An unexpected error occurred.",
                response.getBody().getMessage()
        );
    }
}