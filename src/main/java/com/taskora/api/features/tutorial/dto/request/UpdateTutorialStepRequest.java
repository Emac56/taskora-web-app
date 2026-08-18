package com.taskora.api.features.tutorial.dto.request;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTutorialStepRequest {

    @NotNull(message = "Step number is required.")
    private Integer stepNumber;

    @NotBlank(message = "Instruction is required.")
    @Size(max = 5000, message = "Instruction must not exceed 5000 characters.")
    private String instruction;

    // Optional. Points at an already-hosted image (no upload endpoint yet).
    @URL(message = "Image URL must be a valid URL.")
    @Size(max = 2048, message = "Image URL must not exceed 2048 characters.")
    private String imageUrl;

}
