package com.taskora.api.features.tutorial.dto.response;

import com.taskora.api.features.tutorial.enums.TutorialStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorialResponse {

    private Long id;

    private String title;

    private String description;

    private TutorialStatus status;

}