package com.taskora.api.features.user.controller;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskora.api.common.config.SecurityConfig;
import com.taskora.api.common.enums.Role;
import com.taskora.api.features.tutorial.controller.TutorialController;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.service.TutorialService;
import com.taskora.api.features.user.dto.request.LoginRequest;
import com.taskora.api.features.user.dto.response.LoginResponse;
import com.taskora.api.features.user.service.UserService;
import com.taskora.api.common.ratelimit.LoginRateLimiter;
import com.taskora.api.common.util.ClientIpResolver;

@WebMvcTest({UserController.class, TutorialController.class})
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class LogoutTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private TutorialService tutorialService;

    @MockBean
    private LoginRateLimiter loginRateLimiter;
    
    @MockBean
    private ClientIpResolver clientIpResolver;
    
    private LoginRequest validLoginRequest() {
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

    private CreateTutorialRequest tutorialRequest() {
        CreateTutorialRequest request = new CreateTutorialRequest();
        request.setTitle("Java Basics");
        request.setDescription("Learn Java fundamentals.");
        request.setStatus(TutorialStatus.DRAFT);
        return request;
    }

    private MockHttpSession loginAndGetSession() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(validLoginResponse());

        MvcResult loginResult = mockMvc.perform(
                post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest()))
        )
        .andExpect(status().isOk())
        .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    @Test
    void authenticatedAdminCanLogout() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(
                post("/api/v1/users/logout")
                        .session(session)
        )
        .andExpect(status().isNoContent());
    }

    @Test
    void sessionIsNoLongerAuthenticatedAfterLogout() throws Exception {
        MockHttpSession session = loginAndGetSession();

        mockMvc.perform(
                post("/api/v1/users/logout")
                        .session(session)
        )
        .andExpect(status().isNoContent());

        assertTrue(session.isInvalid());

        mockMvc.perform(
                post("/api/v1/tutorials")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorialRequest()))
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWithoutAuthenticationReturnsNoContent() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/api/v1/users/logout")
        )
        .andExpect(status().isNoContent())
        .andReturn();

        assertNull(result.getRequest().getSession(false));
    }
}