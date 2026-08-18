package com.taskora.api.features.tutorial.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.entity.TutorialStep;

class TutorialStepMapperTest {

    private TutorialStepMapper tutorialStepMapper;

    @BeforeEach
    void setUp() {
        tutorialStepMapper = new TutorialStepMapper();
    }

    @Test
    void shouldMapCreateRequestToEntity() {
        CreateTutorialStepRequest request = new CreateTutorialStepRequest();
        request.setStepNumber(1);
        request.setInstruction("Open the Taskora application.");
        request.setImageUrl("https://storage.example.com/step-1.png");

        TutorialStep tutorialStep = tutorialStepMapper.toEntity(request);

        assertNotNull(tutorialStep);
        assertEquals(1, tutorialStep.getStepNumber());
        assertEquals("Open the Taskora application.", tutorialStep.getInstruction());
        assertEquals("https://storage.example.com/step-1.png", tutorialStep.getImageUrl());
    }

    @Test
    void shouldMapEntityToResponse() {
        TutorialStep tutorialStep = new TutorialStep();
        tutorialStep.setId(1L);
        tutorialStep.setStepNumber(1);
        tutorialStep.setInstruction("Open the Taskora application.");
        tutorialStep.setImageUrl("https://storage.example.com/tutorials/step-1.png");

        TutorialStepResponse response = tutorialStepMapper.toResponse(tutorialStep);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1, response.getStepNumber());
        assertEquals("Open the Taskora application.", response.getInstruction());
        assertEquals("https://storage.example.com/tutorials/step-1.png", response.getImageUrl());
    }

    @Test
    void shouldUpdateTutorialStepEntity() {
        UpdateTutorialStepRequest request = new UpdateTutorialStepRequest();
        request.setStepNumber(2);
        request.setInstruction("Create a Java class.");
        request.setImageUrl("https://storage.example.com/step-2.png");

        TutorialStep tutorialStep = new TutorialStep();
        tutorialStepMapper.updateEntity(request, tutorialStep);

        assertEquals(2, tutorialStep.getStepNumber());
        assertEquals("Create a Java class.", tutorialStep.getInstruction());
        assertEquals("https://storage.example.com/step-2.png", tutorialStep.getImageUrl());
    }
}
