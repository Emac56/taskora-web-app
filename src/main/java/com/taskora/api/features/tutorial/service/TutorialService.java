package com.taskora.api.features.tutorial.service;

import java.util.List;

import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;

public interface TutorialService {

    TutorialResponse create(CreateTutorialRequest request);

    TutorialResponse getById(Long id);

    List<TutorialResponse> getAll();

    TutorialResponse update(Long id, UpdateTutorialRequest request);

    void delete(Long id);
}