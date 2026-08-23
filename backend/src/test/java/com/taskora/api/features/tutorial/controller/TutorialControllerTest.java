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
import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.service.TutorialService;

@WebMvcTest(TutorialController.class)
@AutoConfigureMockMvc(addFilters = false)
class TutorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TutorialService tutorialService;

    @Test
    void shouldCreateTutorial() throws Exception {

        CreateTutorialRequest request = new CreateTutorialRequest();
        request.setTitle("Java Basics");
        request.setDescription("Learn Java fundamentals.");
        request.setStatus(TutorialStatus.DRAFT);

        TutorialResponse response = new TutorialResponse();
        response.setId(1L);
        response.setTitle("Java Basics");
        response.setDescription("Learn Java fundamentals.");
        response.setStatus(TutorialStatus.DRAFT);

        when(tutorialService.create(any(CreateTutorialRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Java Basics"))
        .andExpect(jsonPath("$.description")
                .value("Learn Java fundamentals."))
        .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(tutorialService).create(any(CreateTutorialRequest.class));
    }

    @Test
    void shouldGetTutorialById() throws Exception {

        TutorialResponse response = new TutorialResponse();
        response.setId(1L);
        response.setTitle("Java Basics");
        response.setDescription("Learn Java fundamentals.");
        response.setStatus(TutorialStatus.DRAFT);

        when(tutorialService.getById(1L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/v1/tutorials/1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Java Basics"))
        .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(tutorialService).getById(1L);
    }

    @Test
    void shouldGetAllTutorials() throws Exception {

        TutorialResponse first = new TutorialResponse();
        first.setId(1L);
        first.setTitle("Java Basics");
        first.setDescription("Learn Java fundamentals.");
        first.setStatus(TutorialStatus.DRAFT);

        TutorialResponse second = new TutorialResponse();
        second.setId(2L);
        second.setTitle("Spring Boot Basics");
        second.setDescription("Learn Spring Boot.");
        second.setStatus(TutorialStatus.PUBLISHED);

        when(tutorialService.getAll())
                .thenReturn(List.of(first, second));

        mockMvc.perform(
                get("/api/v1/tutorials")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].title").value("Java Basics"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].title")
                .value("Spring Boot Basics"));

        verify(tutorialService).getAll();
    }

    @Test
    void shouldUpdateTutorial() throws Exception {

        UpdateTutorialRequest request = new UpdateTutorialRequest();
        request.setTitle("Updated Java Basics");
        request.setDescription("Updated description.");
        request.setStatus(TutorialStatus.PUBLISHED);

        TutorialResponse response = new TutorialResponse();
        response.setId(1L);
        response.setTitle("Updated Java Basics");
        response.setDescription("Updated description.");
        response.setStatus(TutorialStatus.PUBLISHED);

        when(tutorialService.update(
                eq(1L),
                any(UpdateTutorialRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/v1/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title")
                .value("Updated Java Basics"))
        .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(tutorialService).update(
                eq(1L),
                any(UpdateTutorialRequest.class));
    }

    @Test
    void shouldDeleteTutorial() throws Exception {

        mockMvc.perform(
                delete("/api/v1/tutorials/1")
        )
        .andExpect(status().isNoContent());

        verify(tutorialService).delete(1L);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingInvalidTutorial()
            throws Exception {

        CreateTutorialRequest request = new CreateTutorialRequest();
        request.setTitle("");
        request.setDescription("");
        request.setStatus(null);

        mockMvc.perform(
                post("/api/v1/tutorials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingInvalidTutorial()
            throws Exception {

        UpdateTutorialRequest request = new UpdateTutorialRequest();
        request.setTitle("");
        request.setDescription("");
        request.setStatus(null);

        mockMvc.perform(
                put("/api/v1/tutorials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
    }
    @Test
void shouldReturnBadRequestWhenCreateTitleIsBlank()
        throws Exception {

    CreateTutorialRequest request = new CreateTutorialRequest();
    request.setTitle("");
    request.setDescription("Valid description.");
    request.setStatus(TutorialStatus.DRAFT);

    mockMvc.perform(
            post("/api/v1/tutorials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isBadRequest());
}
@Test
void shouldReturnBadRequestWhenCreateDescriptionIsBlank()
        throws Exception {

    CreateTutorialRequest request = new CreateTutorialRequest();
    request.setTitle("Java Basics");
    request.setDescription("");
    request.setStatus(TutorialStatus.DRAFT);

    mockMvc.perform(
            post("/api/v1/tutorials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isBadRequest());
}
@Test
void shouldReturnBadRequestWhenUpdateTitleIsBlank()
        throws Exception {

    UpdateTutorialRequest request = new UpdateTutorialRequest();
    request.setTitle("");
    request.setDescription("Valid description.");
    request.setStatus(TutorialStatus.PUBLISHED);

    mockMvc.perform(
            put("/api/v1/tutorials/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isBadRequest());
}

@Test
void shouldReturnBadRequestWhenUpdateDescriptionIsBlank()
        throws Exception {

    UpdateTutorialRequest request = new UpdateTutorialRequest();
    request.setTitle("Java Basics");
    request.setDescription("");
    request.setStatus(TutorialStatus.PUBLISHED);

    mockMvc.perform(
            put("/api/v1/tutorials/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isBadRequest());
}
    @Test
void shouldGetTutorialStats() throws Exception {
    TutorialStatsResponse response =
            new TutorialStatsResponse(5, 3, 2, 17);

    when(tutorialService.getStats()).thenReturn(response);

    mockMvc.perform(get("/api/v1/tutorials/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTutorials").value(5))
            .andExpect(jsonPath("$.publishedCount").value(3))
            .andExpect(jsonPath("$.draftCount").value(2))
            .andExpect(jsonPath("$.totalSteps").value(17));

    verify(tutorialService).getStats();
                                                      }
}
