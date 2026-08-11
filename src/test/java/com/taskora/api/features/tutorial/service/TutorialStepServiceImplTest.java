package com.taskora.api.features.tutorial.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskora.api.common.exception.ResourceNotFoundException;
import com.taskora.api.common.security.CurrentUserProvider;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.entity.TutorialStep;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.mapper.TutorialStepMapper;
import com.taskora.api.features.tutorial.repository.TutorialRepository;
import com.taskora.api.features.tutorial.repository.TutorialStepRepository;

@ExtendWith(MockitoExtension.class)
class TutorialStepServiceImplTest {

    @Mock
    private TutorialStepRepository tutorialStepRepository;

    @Mock
    private TutorialRepository tutorialRepository;

    @Mock
    private TutorialStepMapper tutorialStepMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TutorialStepServiceImpl tutorialStepService;

    private Tutorial tutorial;
    private TutorialStep tutorialStep;
    private TutorialStepResponse response;
    private CreateTutorialStepRequest createRequest;
    private UpdateTutorialStepRequest updateRequest;

    @BeforeEach
    void setUp() {
        tutorial = new Tutorial();
        tutorial.setId(1L);
        tutorial.setTitle("Java Basics");
        tutorial.setStatus(TutorialStatus.PUBLISHED);

        tutorialStep = new TutorialStep();
        tutorialStep.setId(10L);
        tutorialStep.setTutorial(tutorial);
        tutorialStep.setStepNumber(1);
        tutorialStep.setInstruction("Open the project.");
        tutorialStep.setImageUrl("https://example.com/image.png");

        response = new TutorialStepResponse();
        response.setId(10L);
        response.setStepNumber(1);
        response.setInstruction("Open the project.");
        response.setImageUrl("https://example.com/image.png");

        createRequest = new CreateTutorialStepRequest();
        createRequest.setStepNumber(1);
        createRequest.setInstruction("Open the project.");

        updateRequest = new UpdateTutorialStepRequest();
        updateRequest.setStepNumber(2);
        updateRequest.setInstruction("Create a Java class.");
    }

    @Test
    void shouldCreateTutorialStep() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(tutorial));

        when(tutorialStepMapper.toEntity(createRequest))
                .thenReturn(tutorialStep);

        when(tutorialStepRepository.save(tutorialStep))
                .thenReturn(tutorialStep);

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        TutorialStepResponse result =
                tutorialStepService.create(1L, createRequest);

        assertEquals(response, result);

        verify(tutorialRepository).findById(1L);
        verify(tutorialStepMapper).toEntity(createRequest);
        verify(tutorialStepRepository).save(tutorialStep);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }

    @Test
    void shouldThrowExceptionWhenCreatingStepForNonExistingTutorial() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.create(1L, createRequest)
        );

        verify(tutorialRepository).findById(1L);
    }

    @Test
    void shouldGetTutorialStepById() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        TutorialStepResponse result =
                tutorialStepService.getById(10L);

        assertEquals(response, result);

        verify(tutorialStepRepository).findById(10L);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }

    @Test
    void shouldThrowExceptionWhenTutorialStepNotFound() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.getById(10L)
        );

        verify(tutorialStepRepository).findById(10L);
    }

    @Test
    void shouldThrowNotFoundWhenNonAdminRequestsStepOfDraftTutorial() {
        Tutorial draftTutorial = new Tutorial();
        draftTutorial.setId(1L);
        draftTutorial.setStatus(TutorialStatus.DRAFT);

        TutorialStep draftStep = new TutorialStep();
        draftStep.setId(10L);
        draftStep.setTutorial(draftTutorial);

        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(draftStep));
        when(currentUserProvider.isAdmin()).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.getById(10L)
        );
    }

    @Test
    void shouldGetAllTutorialStepsByTutorialId() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(tutorial));

        when(tutorialStepRepository.findAllByTutorialId(1L))
                .thenReturn(List.of(tutorialStep));

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        List<TutorialStepResponse> result =
                tutorialStepService.getAllByTutorialId(1L);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(tutorialRepository).findById(1L);
        verify(tutorialStepRepository).findAllByTutorialId(1L);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }

    @Test
    void shouldThrowNotFoundWhenNonAdminRequestsStepsOfDraftTutorial() {
        Tutorial draftTutorial = new Tutorial();
        draftTutorial.setId(1L);
        draftTutorial.setStatus(TutorialStatus.DRAFT);

        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(draftTutorial));
        when(currentUserProvider.isAdmin()).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.getAllByTutorialId(1L)
        );
    }

    @Test
    void shouldUpdateTutorialStep() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));

        when(tutorialStepRepository.save(tutorialStep))
                .thenReturn(tutorialStep);

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        TutorialStepResponse result =
                tutorialStepService.update(10L, updateRequest);

        assertEquals(response, result);

        verify(tutorialStepRepository).findById(10L);
        verify(tutorialStepMapper)
                .updateEntity(updateRequest, tutorialStep);
        verify(tutorialStepRepository).save(tutorialStep);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTutorialStep() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.update(10L, updateRequest)
        );

        verify(tutorialStepRepository).findById(10L);
    }

    @Test
    void shouldDeleteTutorialStep() {
        when(tutorialStepRepository.existsById(10L))
                .thenReturn(true);

        tutorialStepService.delete(10L);

        verify(tutorialStepRepository).existsById(10L);
        verify(tutorialStepRepository).deleteById(10L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTutorialStep() {
        when(tutorialStepRepository.existsById(10L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.delete(10L)
        );

        verify(tutorialStepRepository).existsById(10L);
    }
    
    @Test
    void shouldThrowExceptionWhenTutorialNotFoundForGetAllByTutorialId() {
    when(tutorialRepository.findById(1L))
            .thenReturn(Optional.empty());

    assertThrows(
            ResourceNotFoundException.class,
            () -> tutorialStepService.getAllByTutorialId(1L)
    );

    verify(tutorialRepository).findById(1L);
}
}