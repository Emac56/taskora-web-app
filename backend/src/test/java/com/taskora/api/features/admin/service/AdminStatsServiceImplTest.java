package com.taskora.api.features.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskora.api.features.admin.dto.response.AdminDashboardStatsResponse;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.repository.TutorialRepository;
import com.taskora.api.features.tutorial.repository.TutorialStepRepository;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceImplTest {

    @Mock
    private TutorialRepository tutorialRepository;

    @Mock
    private TutorialStepRepository tutorialStepRepository;

    private AdminStatsServiceImpl adminStatsService;

    @BeforeEach
    void setUp() {
        adminStatsService = new AdminStatsServiceImpl(
                tutorialRepository, tutorialStepRepository);
    }

    @Test
    void shouldAggregateStatsFromCountQueries() {
        when(tutorialRepository.count()).thenReturn(5L);
        when(tutorialRepository.countByStatus(TutorialStatus.PUBLISHED))
                .thenReturn(3L);
        when(tutorialRepository.countByStatus(TutorialStatus.DRAFT))
                .thenReturn(2L);
        when(tutorialStepRepository.count()).thenReturn(21L);

        AdminDashboardStatsResponse response =
                adminStatsService.getDashboardStats();

        assertEquals(5L, response.getTotalTutorials());
        assertEquals(3L, response.getPublishedCount());
        assertEquals(2L, response.getDraftCount());
        assertEquals(21L, response.getTotalSteps());

        // Confirms the service only ever asks for counts — never
        // findAll() — so it can't regress back into loading full
        // entity lists into memory.
        verify(tutorialRepository).count();
        verify(tutorialRepository).countByStatus(TutorialStatus.PUBLISHED);
        verify(tutorialRepository).countByStatus(TutorialStatus.DRAFT);
        verify(tutorialStepRepository).count();
        verifyNoMoreInteractions(tutorialRepository, tutorialStepRepository);
    }

    @Test
    void shouldReturnZeroedStatsForEmptyDataset() {
        when(tutorialRepository.count()).thenReturn(0L);
        when(tutorialRepository.countByStatus(TutorialStatus.PUBLISHED))
                .thenReturn(0L);
        when(tutorialRepository.countByStatus(TutorialStatus.DRAFT))
                .thenReturn(0L);
        when(tutorialStepRepository.count()).thenReturn(0L);

        AdminDashboardStatsResponse response =
                adminStatsService.getDashboardStats();

        assertEquals(0L, response.getTotalTutorials());
        assertEquals(0L, response.getPublishedCount());
        assertEquals(0L, response.getDraftCount());
        assertEquals(0L, response.getTotalSteps());
    }
  }
