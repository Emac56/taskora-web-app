package com.taskora.api.common.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskora.api.features.tutorial.controller.TutorialController;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.service.TutorialService;

/**
 * Verifies the CSRF fix for BE (SecurityConfig disabled CSRF while relying
 * on session-cookie auth + allowCredentials(true)). Covers both directions:
 * request rejected without a token, accepted with one.
 */
@WebMvcTest(TutorialController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class CsrfProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TutorialService tutorialService;

    private CreateTutorialRequest sampleRequest() {
        CreateTutorialRequest request = new CreateTutorialRequest();
        request.setTitle("Java Basics");
        request.setDescription("Learn Java fundamentals.");
        request.setStatus(TutorialStatus.DRAFT);
        return request;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTutorialShouldBeRejectedWithoutCsrfToken() throws Exception {

        mockMvc.perform(
                post("/api/v1/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest()))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTutorialShouldSucceedWithCsrfToken() throws Exception {

        TutorialResponse response = new TutorialResponse();
        response.setId(1L);
        response.setTitle("Java Basics");

        when(tutorialService.create(any(CreateTutorialRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/tutorials")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest()))
        )
        .andExpect(status().isOk());
    }
}
