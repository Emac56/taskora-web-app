package com.taskora.api.features.tutorial.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorialStatsResponse {

    private long totalTutorials;
    private long publishedCount;
    private long draftCount;
    private long totalSteps;

    public TutorialStatsResponse(long totalTutorials, long publishedCount,
            long draftCount, long totalSteps) {
        this.totalTutorials = totalTutorials;
        this.publishedCount = publishedCount;
        this.draftCount = draftCount;
        this.totalSteps = totalSteps;
    }
}
