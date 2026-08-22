package com.taskora.api.features.tutorial.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplaceTutorialStepsRequest {

    @NotEmpty(message = "At least one step is required.")
    @Valid
    private List<ReplaceTutorialStepItem> steps;

}
