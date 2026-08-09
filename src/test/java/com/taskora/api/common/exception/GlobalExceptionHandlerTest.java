package com.taskora.api.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
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
}