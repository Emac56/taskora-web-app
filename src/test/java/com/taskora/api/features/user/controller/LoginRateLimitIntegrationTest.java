package com.taskora.api.features.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskora.api.common.config.SecurityConfig;
import com.taskora.api.common.enums.Role;
import com.taskora.api.common.ratelimit.LoginRateLimiter;
import com.taskora.api.common.util.ClientIpResolver;
import com.taskora.api.features.user.dto.request.LoginRequest;
import com.taskora.api.features.user.dto.response.LoginResponse;
import com.taskora.api.features.user.service.UserService;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, LoginRateLimiter.class, ClientIpResolver.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.rate-limit.login.max-attempts=5",
        "app.rate-limit.login.window-seconds=60"
})
class LoginRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@taskora.com");
        request.setPassword("password");
        return request;
    }

    private LoginResponse validLoginResponse() {
        LoginResponse response = new LoginResponse();
        response.setId(1L);
        response.setName("Admin");
        response.setEmail("admin@taskora.com");
        response.setRole(Role.ADMIN);
        return response;
    }

    @Test
    void shouldAllowLoginsWithinLimitAndRejectAfterExceeding() throws Exception {

        when(userService.login(any(LoginRequest.class)))
                .thenReturn(validLoginResponse());

        // Attempt 1 — within limit, should succeed
        mockMvc.perform(
                post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest()))
        )
        .andExpect(status().isOk());

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid credentials."));

        // Attempts 2-5 — still within limit (5 max), rejected only due to wrong credentials
        for (int attempt = 2; attempt <= 5; attempt++) {
            mockMvc.perform(
                    post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest()))
            )
            .andExpect(status().isUnauthorized());
        }

        // Attempt 6 — exceeds the limit, should be rate limited
        mockMvc.perform(
                post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest()))
        )
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }
}