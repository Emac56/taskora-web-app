package com.taskora.api.common.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void shouldSetAndGetApiResponseValues() {

        ApiResponse<String> response = new ApiResponse<>();

        response.setSuccess(true);
        response.setMessage("Success.");
        response.setData("Test data");

        assertTrue(response.isSuccess());
        assertEquals("Success.", response.getMessage());
        assertEquals("Test data", response.getData());
    }
}