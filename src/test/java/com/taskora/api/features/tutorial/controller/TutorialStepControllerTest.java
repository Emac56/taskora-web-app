package com.taskora.api.features.tutorial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.service.TutorialStepService;

@WebMvcTest(TutorialStepController.class)
@AutoConfigureMockMvc(addFilters = false)
class TutorialStepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TutorialStepService tutorialStepService;

    @Test
    void shouldCreateTutorialStep() throws Exception {

        CreateTutorialStepRequest request =
                new CreateTutorialStepRequest();

        request.setStepNumber(1);
        request.setInstruction("Open the project.");

        TutorialStepResponse response =
                new TutorialStepResponse();

        response.setId(10L);
        response.setStepNumber(1);
        response.setInstruction("Open the project.");
        response.setImageUrl("https://example.com/image.png");

        when(tutorialStepService.create(
                eq(1L),
                any(CreateTutorialStepRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/tutorials/1/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.stepNumber").value(1))
        .andExpect(jsonPath("$.instruction")
                .value("Open the project."))
        .andExpect(jsonPath("$.imageUrl")
                .value("https://example.com/image.png"));

        verify(tutorialStepService).create(
                eq(1L),
                any(CreateTutorialStepRequest.class));
    }

    @Test
    void shouldGetTutorialStepById() throws Exception {

        TutorialStepResponse response =
                new TutorialStepResponse();

        response.setId(10L);
        response.setStepNumber(1);
        response.setInstruction("Open the project.");
        response.setImageUrl("https://example.com/image.png");

        when(tutorialStepService.getById(10L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/v1/tutorial-steps/10")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.stepNumber").value(1))
        .andExpect(jsonPath("$.instruction")
                .value("Open the project."))
        .andExpect(jsonPath("$.imageUrl")
                .value("https://example.com/image.png"));

        verify(tutorialStepService).getById(10L);
    }

    @Test
    void shouldGetAllTutorialStepsByTutorialId() throws Exception {

        TutorialStepResponse first =
                new TutorialStepResponse();

        first.setId(10L);
        first.setStepNumber(1);
        first.setInstruction("Open the project.");

        TutorialStepResponse second =
                new TutorialStepResponse();

        second.setId(11L);
        second.setStepNumber(2);
        second.setInstruction("Create a Java class.");

        when(tutorialStepService.getAllByTutorialId(1L))
                .thenReturn(List.of(first, second));

        mockMvc.perform(
                get("/api/v1/tutorials/1/steps")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].stepNumber").value(1))
        .andExpect(jsonPath("$[1].id").value(11))
        .andExpect(jsonPath("$[1].stepNumber").value(2));

        verify(tutorialStepService)
                .getAllByTutorialId(1L);
    }

    @Test
    void shouldUpdateTutorialStep() throws Exception {

        UpdateTutorialStepRequest request =
                new UpdateTutorialStepRequest();

        request.setStepNumber(2);
        request.setInstruction("Create a Java class.");

        TutorialStepResponse response =
                new TutorialStepResponse();

        response.setId(10L);
        response.setStepNumber(2);
        response.setInstruction("Create a Java class.");

        when(tutorialStepService.update(
                eq(10L),
                any(UpdateTutorialStepRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/v1/tutorial-steps/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.stepNumber").value(2))
        .andExpect(jsonPath("$.instruction")
                .value("Create a Java class."));

        verify(tutorialStepService).update(
                eq(10L),
                any(UpdateTutorialStepRequest.class));
    }

    @Test
    void shouldDeleteTutorialStep() throws Exception {

        mockMvc.perform(
                delete("/api/v1/tutorial-steps/10")
        )
        .andExpect(status().isNoContent());

        verify(tutorialStepService).delete(10L);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingInvalidTutorialStep()
            throws Exception {

        CreateTutorialStepRequest request =
                new CreateTutorialStepRequest();

        request.setStepNumber(null);
        request.setInstruction("");

        mockMvc.perform(
                post("/api/v1/tutorials/1/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingInvalidTutorialStep()
            throws Exception {

        UpdateTutorialStepRequest request =
                new UpdateTutorialStepRequest();

        request.setStepNumber(null);
        request.setInstruction("");

        mockMvc.perform(
                put("/api/v1/tutorial-steps/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }
}