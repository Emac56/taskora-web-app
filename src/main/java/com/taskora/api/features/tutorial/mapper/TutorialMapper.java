package com.taskora.api.features.tutorial.mapper;

import org.springframework.stereotype.Component;

import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;

@Component
public class TutorialMapper {

    public Tutorial toEntity(CreateTutorialRequest request) {
        Tutorial tutorial = new Tutorial();

        tutorial.setTitle(request.getTitle());
        tutorial.setDescription(request.getDescription());
        tutorial.setStatus(request.getStatus());

        return tutorial;
    }

    public void updateEntity(UpdateTutorialRequest request, Tutorial tutorial) {
        tutorial.setTitle(request.getTitle());
        tutorial.setDescription(request.getDescription());
        tutorial.setStatus(request.getStatus());
    }

    public TutorialResponse toResponse(Tutorial tutorial) {
        TutorialResponse response = new TutorialResponse();

        response.setId(tutorial.getId());
        response.setTitle(tutorial.getTitle());
        response.setDescription(tutorial.getDescription());
        response.setStatus(tutorial.getStatus());

        return response;
    }
}