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

import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.mapper.TutorialMapper;
import com.taskora.api.features.tutorial.repository.TutorialRepository;

@ExtendWith(MockitoExtension.class)
class TutorialServiceImplTest {

    @Mock
    private TutorialRepository tutorialRepository;

    @Mock
    private TutorialMapper tutorialMapper;

    @InjectMocks
    private TutorialServiceImpl tutorialService;

    private Tutorial tutorial;
    private TutorialResponse tutorialResponse;
    private CreateTutorialRequest createRequest;
    private UpdateTutorialRequest updateRequest;

    @BeforeEach
    void setUp() {
        tutorial = new Tutorial();
        tutorial.setId(1L);
        tutorial.setTitle("Java Basics");
        tutorial.setDescription("Learn Java fundamentals.");
        tutorial.setStatus(TutorialStatus.DRAFT);

        tutorialResponse = new TutorialResponse();
        tutorialResponse.setId(1L);
        tutorialResponse.setTitle("Java Basics");
        tutorialResponse.setDescription("Learn Java fundamentals.");
        tutorialResponse.setStatus(TutorialStatus.DRAFT);

        createRequest = new CreateTutorialRequest();
        createRequest.setTitle("Java Basics");
        createRequest.setDescription("Learn Java fundamentals.");
        createRequest.setStatus(TutorialStatus.DRAFT);

        updateRequest = new UpdateTutorialRequest();
        updateRequest.setTitle("Advanced Java");
        updateRequest.setDescription("Learn advanced Java.");
        updateRequest.setStatus(TutorialStatus.PUBLISHED);
    }

    @Test
    void shouldCreateTutorial() {
        when(tutorialMapper.toEntity(createRequest))
                .thenReturn(tutorial);

        when(tutorialRepository.save(tutorial))
                .thenReturn(tutorial);

        when(tutorialMapper.toResponse(tutorial))
                .thenReturn(tutorialResponse);

        TutorialResponse result = tutorialService.create(createRequest);

        assertEquals(tutorialResponse, result);

        verify(tutorialMapper).toEntity(createRequest);
        verify(tutorialRepository).save(tutorial);
        verify(tutorialMapper).toResponse(tutorial);
    }

    @Test
    void shouldGetTutorialById() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(tutorial));

        when(tutorialMapper.toResponse(tutorial))
                .thenReturn(tutorialResponse);

        TutorialResponse result = tutorialService.getById(1L);

        assertEquals(tutorialResponse, result);

        verify(tutorialRepository).findById(1L);
        verify(tutorialMapper).toResponse(tutorial);
    }

    @Test
    void shouldThrowExceptionWhenTutorialNotFoundById() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialService.getById(1L)
        );

        verify(tutorialRepository).findById(1L);
    }

    @Test
    void shouldGetAllTutorials() {
        Tutorial secondTutorial = new Tutorial();
        secondTutorial.setId(2L);
        secondTutorial.setTitle("Spring Boot");

        TutorialResponse secondResponse = new TutorialResponse();
        secondResponse.setId(2L);
        secondResponse.setTitle("Spring Boot");

        when(tutorialRepository.findAll())
                .thenReturn(List.of(tutorial, secondTutorial));

        when(tutorialMapper.toResponse(tutorial))
                .thenReturn(tutorialResponse);

        when(tutorialMapper.toResponse(secondTutorial))
                .thenReturn(secondResponse);

        List<TutorialResponse> result = tutorialService.getAll();

        assertEquals(2, result.size());
        assertEquals(tutorialResponse, result.get(0));
        assertEquals(secondResponse, result.get(1));

        verify(tutorialRepository).findAll();
        verify(tutorialMapper).toResponse(tutorial);
        verify(tutorialMapper).toResponse(secondTutorial);
    }

    @Test
    void shouldUpdateTutorial() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(tutorial));

        when(tutorialRepository.save(tutorial))
                .thenReturn(tutorial);

        when(tutorialMapper.toResponse(tutorial))
                .thenReturn(tutorialResponse);

        TutorialResponse result =
                tutorialService.update(1L, updateRequest);

        assertEquals(tutorialResponse, result);

        verify(tutorialRepository).findById(1L);
        verify(tutorialMapper).updateEntity(updateRequest, tutorial);
        verify(tutorialRepository).save(tutorial);
        verify(tutorialMapper).toResponse(tutorial);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTutorial() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
               ResourceNotFoundException.class,
                () -> tutorialService.update(1L, updateRequest)
        );

        verify(tutorialRepository).findById(1L);
    }

    @Test
    void shouldDeleteTutorial() {
        when(tutorialRepository.existsById(1L))
                .thenReturn(true);

        tutorialService.delete(1L);

        verify(tutorialRepository).existsById(1L);
        verify(tutorialRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTutorial() {
        when(tutorialRepository.existsById(1L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialService.delete(1L)
        );

        verify(tutorialRepository).existsById(1L);
    }
}