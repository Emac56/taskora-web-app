package com.taskora.api.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.taskora.api.common.dto.response.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception) {

        ApiErrorResponse response = new ApiErrorResponse();

        response.setSuccess(false);
        response.setMessage(exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception) {

        ApiErrorResponse response = new ApiErrorResponse();

        response.setSuccess(false);
        response.setMessage(exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException exception) {

        ApiErrorResponse response = new ApiErrorResponse();

        response.setSuccess(false);
        response.setMessage(exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER,
                        String.valueOf(exception.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFile(
            InvalidFileException exception) {

        ApiErrorResponse response = new ApiErrorResponse();
        response.setSuccess(false);
        response.setMessage(exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception) {

        ApiErrorResponse response = new ApiErrorResponse();
        response.setSuccess(false);
        response.setMessage("Uploaded file is too large.");

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ApiErrorResponse> handleImageUpload(
            ImageUploadException exception) {

        ApiErrorResponse response = new ApiErrorResponse();
        response.setSuccess(false);
        response.setMessage(exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {

        ApiErrorResponse response = new ApiErrorResponse();
        response.setSuccess(false);
        response.setMessage(exception.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception exception) {

        // Log the real exception server-side - the client only ever sees
        // the generic message below, but we need this to debug.
        log.error("Unhandled exception", exception);

        ApiErrorResponse response = new ApiErrorResponse();
        response.setSuccess(false);
        response.setMessage("An unexpected error occurred.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
