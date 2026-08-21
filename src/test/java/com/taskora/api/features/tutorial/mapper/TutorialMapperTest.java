package com.taskora.api.features.tutorial.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.enums.TutorialStatus;

class TutorialMapperTest {

    private TutorialMapper tutorialMapper;

    @BeforeEach
    void setUp() {
        tutorialMapper = new TutorialMapper();
    }

    @Test
    void shouldMapCreateRequestToEntity() {
        CreateTutorialRequest request = new CreateTutorialRequest();

        request.setTitle("Java Basics");
        request.setDescription("Learn Java fundamentals.");
        request.setStatus(TutorialStatus.DRAFT);

        Tutorial tutorial = tutorialMapper.toEntity(request);

        assertNotNull(tutorial);
        assertEquals("Java Basics", tutorial.getTitle());
        assertEquals("Learn Java fundamentals.", tutorial.getDescription());
        assertEquals(TutorialStatus.DRAFT, tutorial.getStatus());
    }

    @Test
    void shouldUpdateEntityFromUpdateRequest() {
        Tutorial tutorial = new Tutorial();

        UpdateTutorialRequest request = new UpdateTutorialRequest();

        request.setTitle("Updated Java Basics");
        request.setDescription("Updated Java fundamentals.");
        request.setStatus(TutorialStatus.PUBLISHED);

        tutorialMapper.updateEntity(request, tutorial);

        assertEquals("Updated Java Basics", tutorial.getTitle());
        assertEquals("Updated Java fundamentals.", tutorial.getDescription());
        assertEquals(TutorialStatus.PUBLISHED, tutorial.getStatus());
    }

    @Test
    void shouldMapEntityToResponse() {
        Tutorial tutorial = new Tutorial();

        tutorial.setId(1L);
        tutorial.setTitle("Java Basics");
        tutorial.setDescription("Learn Java fundamentals.");
        tutorial.setStatus(TutorialStatus.PUBLISHED);

        TutorialResponse response = tutorialMapper.toResponse(tutorial);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java Basics", response.getTitle());
        assertEquals("Learn Java fundamentals.", response.getDescription());
        assertEquals(TutorialStatus.PUBLISHED, response.getStatus());
    }
}