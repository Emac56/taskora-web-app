package com.taskora.api.features.tutorial.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorialStepResponse {

    private Long id;

    private Integer stepNumber;

    private String instruction;

    private String imageUrl;

}