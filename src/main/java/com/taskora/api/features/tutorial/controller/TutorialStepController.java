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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.taskora.api.common.storage.SupabaseStorageClient;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.ImageUploadResponse;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.service.TutorialStepService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TutorialStepController {

    private final TutorialStepService tutorialStepService;
    private final SupabaseStorageClient supabaseStorageClient;

    public TutorialStepController(
            TutorialStepService tutorialStepService,
            SupabaseStorageClient supabaseStorageClient) {
        this.tutorialStepService = tutorialStepService;
        this.supabaseStorageClient = supabaseStorageClient;
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

    // Uploads an image to Supabase Storage and returns its public URL.
    // The frontend calls this first, then passes the returned imageUrl
    // into the create/update step request above.
    @PostMapping("/tutorial-steps/images")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file) {

        String imageUrl = supabaseStorageClient.upload(file);

        return ResponseEntity.ok(new ImageUploadResponse(imageUrl));
    }
}
