package com.taskora.api.common.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

@WebMvcTest(TutorialController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TutorialService tutorialService;

    @Test
    void publicGetTutorialsShouldBeAccessibleWithoutAuth() throws Exception {

        when(tutorialService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tutorials"))
                .andExpect(status().isOk());
    }

    @Test
    void createTutorialShouldBeRejectedWithoutAuth() throws Exception {

        CreateTutorialRequest request = new CreateTutorialRequest();
        request.setTitle("Java Basics");
        request.setDescription("Learn Java fundamentals.");
        request.setStatus(TutorialStatus.DRAFT);

        mockMvc.perform(
                post("/api/v1/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTutorialShouldSucceedWhenAuthenticated() throws Exception {

        CreateTutorialRequest request = new CreateTutorialRequest();
        request.setTitle("Java Basics");
        request.setDescription("Learn Java fundamentals.");
        request.setStatus(TutorialStatus.DRAFT);

        TutorialResponse response = new TutorialResponse();
        response.setId(1L);
        response.setTitle("Java Basics");

        when(tutorialService.create(any(CreateTutorialRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk());
    }
}