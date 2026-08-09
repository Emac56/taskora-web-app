package com.taskora.api.features.tutorial.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.service.TutorialStepService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TutorialStepController {

    private final TutorialStepService tutorialStepService;

    public TutorialStepController(TutorialStepService tutorialStepService) {
        this.tutorialStepService = tutorialStepService;
    }

    @PostMapping("/tutorials/{tutorialId}/steps")
    public ResponseEntity<TutorialStepResponse> create(
            @PathVariable Long tutorialId,
            @Valid @RequestBody CreateTutorialStepRequest request) {

        TutorialStepResponse response =
                tutorialStepService.create(tutorialId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tutorial-steps/{id}")
    public ResponseEntity<TutorialStepResponse> getById(
            @PathVariable Long id) {

        TutorialStepResponse response =
                tutorialStepService.getById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tutorials/{tutorialId}/steps")
    public ResponseEntity<List<TutorialStepResponse>> getAllByTutorialId(
            @PathVariable Long tutorialId) {

        List<TutorialStepResponse> response =
                tutorialStepService.getAllByTutorialId(tutorialId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/tutorial-steps/{id}")
    public ResponseEntity<TutorialStepResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTutorialStepRequest request) {

        TutorialStepResponse response =
                tutorialStepService.update(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tutorial-steps/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        tutorialStepService.delete(id);

        return ResponseEntity.noContent().build();
    }
}