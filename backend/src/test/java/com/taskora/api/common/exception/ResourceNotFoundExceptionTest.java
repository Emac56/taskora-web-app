package com.taskora.api.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Tutorial not found.");

        assertEquals(
                "Tutorial not found.",
                exception.getMessage()
        );
    }
}