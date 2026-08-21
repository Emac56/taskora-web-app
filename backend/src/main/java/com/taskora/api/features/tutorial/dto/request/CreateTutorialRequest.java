package com.taskora.api.features.tutorial.dto.request;

import com.taskora.api.features.tutorial.enums.TutorialStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTutorialRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 255, message = "Title must not exceed 255 characters.")
    private String title;

    @NotBlank(message = "Description is required.")
    @Size(max = 5000, message = "Description must not exceed 5000 characters.")
    private String description;

    @NotNull(message = "Status is required.")
    private TutorialStatus status;

}