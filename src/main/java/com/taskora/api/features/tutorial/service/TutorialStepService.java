package com.taskora.api.features.tutorial.service;

import java.util.List;

import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;

public interface TutorialStepService {

    TutorialStepResponse create(
            Long tutorialId,
            CreateTutorialStepRequest request
    );

    TutorialStepResponse getById(Long id);

    List<TutorialStepResponse> getAllByTutorialId(Long tutorialId);

    TutorialStepResponse update(
            Long id,
            UpdateTutorialStepRequest request
    );

    void delete(Long id);
}