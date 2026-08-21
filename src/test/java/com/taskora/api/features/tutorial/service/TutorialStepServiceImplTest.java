@Test
    void shouldGetAllTutorialStepsByTutorialId() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(tutorial));

        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(tutorialStep));

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        List<TutorialStepResponse> result =
                tutorialStepService.getAllByTutorialId(1L);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(tutorialRepository).findById(1L);
        verify(tutorialStepRepository).findAllByTutorialIdOrderByStepNumberAsc(1L);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }
