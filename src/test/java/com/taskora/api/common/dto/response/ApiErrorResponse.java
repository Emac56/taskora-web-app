package com.taskora.api.common.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class ApiErrorResponseTest {

    @Test
    void shouldSetAndGetApiErrorResponseValues() {

        ApiErrorResponse response = new ApiErrorResponse();

        response.setSuccess(false);
        response.setMessage("Something went wrong.");

        assertFalse(response.isSuccess());
        assertEquals(
                "Something went wrong.",
                response.getMessage()
        );
    }
}