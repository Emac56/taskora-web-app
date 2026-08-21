package com.taskora.api.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.taskora.api.common.dto.response.ApiErrorResponse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Tutorial not found.");

        ResponseEntity<ApiErrorResponse> response = handler.handleResourceNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Tutorial not found.", response.getBody().getMessage());
    }

    @Test
    void shouldHandleInvalidCredentialsException() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid username or password.");

        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidCredentials(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Invalid username or password.", response.getBody().getMessage());
    }

    @Test
    void shouldHandleRateLimitExceededException() {
        RateLimitExceededException exception = new RateLimitExceededException("Too many login attempts.", 30L);

        ResponseEntity<ApiErrorResponse> response = handler.handleRateLimitExceeded(exception);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Too many login attempts.", response.getBody().getMessage());
        assertEquals("30", response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void shouldNotSetRetryAfterHeaderWhenNotRateLimited() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Tutorial not found.");

        ResponseEntity<ApiErrorResponse> response = handler.handleResourceNotFound(exception);

        assertNull(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void shouldHandleInvalidFileException() {
        InvalidFileException exception = new InvalidFileException("Image must be PNG, JPEG, or WEBP.");

        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidFile(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Image must be PNG, JPEG, or WEBP.", response.getBody().getMessage());
    }

    @Test
    void shouldHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(5L * 1024 * 1024);

        ResponseEntity<ApiErrorResponse> response = handler.handleMaxUploadSizeExceeded(exception);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Uploaded file is too large.", response.getBody().getMessage());
    }

    @Test
    void shouldHandleImageUploadException() {
        ImageUploadException exception = new ImageUploadException("Failed to upload image to storage.");

        ResponseEntity<ApiErrorResponse> response = handler.handleImageUpload(exception);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Failed to upload image to storage.", response.getBody().getMessage());
    }

    // NEW: covers handleDuplicateStepNumber, previously untested (SonarCloud
    // flagged lines 96-106 in GlobalExceptionHandler.java as uncovered).
    @Test
    void shouldHandleDuplicateStepNumberException() {
        DuplicateStepNumberException exception =
                new DuplicateStepNumberException("Step number 1 already exists for this tutorial.");

        ResponseEntity<ApiErrorResponse> response = handler.handleDuplicateStepNumber(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals(
                "Step number 1 already exists for this tutorial.",
                response.getBody().getMessage());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        FieldError fieldError = new FieldError("loginRequest", "email", "Invalid email format.");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("email: Invalid email format.", response.getBody().getMessage());
    }

    @Test
    void shouldHandleGenericException() {
        Exception exception = new RuntimeException("Something broke.");

        ResponseEntity<ApiErrorResponse> response = handler.handleGeneric(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("An unexpected error occurred.", response.getBody().getMessage());
    }
}
