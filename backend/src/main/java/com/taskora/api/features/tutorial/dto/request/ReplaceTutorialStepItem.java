package com.taskora.api.features.tutorial.dto.request;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplaceTutorialStepItem {

    // Null = create a new step. Non-null must belong to the target
    // tutorial — enforced in the service layer, never trust the client.
    private Long id;

    @NotNull(message = "Step number is required.")
    @Min(value = 1, message = "Step number must be at least 1.")
    private Integer stepNumber;

    @NotBlank(message = "Instruction is required.")
    @Size(max = 5000, message = "Instruction must not exceed 5000 characters.")
    private String instruction;

    @URL(message = "Image URL must be a valid URL.")
    @Size(max = 2048, message = "Image URL must not exceed 2048 characters.")
    private String imageUrl;

}
