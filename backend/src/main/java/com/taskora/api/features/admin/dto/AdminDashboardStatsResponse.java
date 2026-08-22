package com.taskora.api.features.admin.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDashboardStatsResponse {

    private long totalTutorials;
    private long publishedCount;
    private long draftCount;
    private long totalSteps;
}
