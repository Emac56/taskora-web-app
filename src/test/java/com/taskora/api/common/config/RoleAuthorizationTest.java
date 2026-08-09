package com.taskora.api.common.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.taskora.api.features.tutorial.controller.TutorialStepController;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.service.TutorialService;
import com.taskora.api.features.tutorial.service.TutorialStepService;

@WebMvcTest({TutorialController.class, TutorialStepController.class})
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class RoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TutorialService tutorialService;

    @MockBean
    private TutorialStepService tutorialStepService;

    private CreateTutorialRequest createTutorialRequest() {
        CreateTutorialRequest request = new CreateTutorialRequest();
        request.setTitle("Java Basics");
        request.setDescription("Learn Java fundamentals.");
        request.setStatus(TutorialStatus.DRAFT);
        return request;
    }

    private UpdateTutorialRequest updateTutorialRequest() {
        UpdateTutorialRequest request = new UpdateTutorialRequest();
        request.setTitle("Updated Java Basics");
        request.setDescription("Updated description.");
        request.setStatus(TutorialStatus.PUBLISHED);
        return request;
    }

    private CreateTutorialStepRequest createStepRequest() {
        CreateTutorialStepRequest request = new CreateTutorialStepRequest();
        request.setStepNumber(1);
        request.setInstruction("Open the project.");
        return request;
    }

    private UpdateTutorialStepRequest updateStepRequest() {
        UpdateTutorialStepRequest request = new UpdateTutorialStepRequest();
        request.setStepNumber(2);
        request.setInstruction("Create a Java class.");
        return request;
    }

    // ---------- GET tutorials: ADMIN + CLIENT allowed ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanGetTutorials() throws Exception {
        mockMvc.perform(get("/api/v1/tutorials"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCanGetTutorials() throws Exception {
        mockMvc.perform(get("/api/v1/tutorials"))
                .andExpect(status().isOk());
    }

    // ---------- POST tutorial: ADMIN only ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanPostTutorial() throws Exception {
        TutorialResponse response = new TutorialResponse();
        response.setId(1L);

        when(tutorialService.create(any(CreateTutorialRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTutorialRequest()))
        )
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotPostTutorial() throws Exception {
        mockMvc.perform(
                post("/api/v1/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTutorialRequest()))
        )
        .andExpect(status().isForbidden());
    }

    // ---------- PUT tutorial: ADMIN only ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanPutTutorial() throws Exception {
        TutorialResponse response = new TutorialResponse();
        response.setId(1L);

        when(tutorialService.update(eq(1L), any(UpdateTutorialRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/v1/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTutorialRequest()))
        )
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotPutTutorial() throws Exception {
        mockMvc.perform(
                put("/api/v1/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTutorialRequest()))
        )
        .andExpect(status().isForbidden());
    }

    // ---------- DELETE tutorial: ADMIN only ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteTutorial() throws Exception {
        mockMvc.perform(delete("/api/v1/tutorials/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotDeleteTutorial() throws Exception {
        mockMvc.perform(delete("/api/v1/tutorials/1"))
                .andExpect(status().isForbidden());
    }

    // ---------- GET tutorial steps: ADMIN + CLIENT allowed ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanGetTutorialSteps() throws Exception {
        mockMvc.perform(get("/api/v1/tutorials/1/steps"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCanGetTutorialSteps() throws Exception {
        mockMvc.perform(get("/api/v1/tutorials/1/steps"))
                .andExpect(status().isOk());
    }

    // ---------- POST tutorial step: ADMIN only ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanPostTutorialStep() throws Exception {
        TutorialStepResponse response = new TutorialStepResponse();
        response.setId(10L);

        when(tutorialStepService.create(eq(1L), any(CreateTutorialStepRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/tutorials/1/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createStepRequest()))
        )
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotPostTutorialStep() throws Exception {
        mockMvc.perform(
                post("/api/v1/tutorials/1/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createStepRequest()))
        )
        .andExpect(status().isForbidden());
    }

    // ---------- PUT tutorial step: ADMIN only ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanPutTutorialStep() throws Exception {
        TutorialStepResponse response = new TutorialStepResponse();
        response.setId(10L);

        when(tutorialStepService.update(eq(10L), any(UpdateTutorialStepRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/v1/tutorial-steps/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStepRequest()))
        )
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotPutTutorialStep() throws Exception {
        mockMvc.perform(
                put("/api/v1/tutorial-steps/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStepRequest()))
        )
        .andExpect(status().isForbidden());
    }

    // ---------- DELETE tutorial step: ADMIN only ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteTutorialStep() throws Exception {
        mockMvc.perform(delete("/api/v1/tutorial-steps/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotDeleteTutorialStep() throws Exception {
        mockMvc.perform(delete("/api/v1/tutorial-steps/10"))
                .andExpect(status().isForbidden());
    }

    // ---------- Unauthenticated ----------

    @Test
    void unauthenticatedRequestToProtectedEndpointIsRejected() throws Exception {
        mockMvc.perform(
                post("/api/v1/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTutorialRequest()))
        )
        .andExpect(status().isUnauthorized());
    }
}