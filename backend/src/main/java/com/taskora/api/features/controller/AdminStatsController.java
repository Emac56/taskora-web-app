package com.taskora.api.features.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskora.api.features.admin.dto.response.AdminDashboardStatsResponse;
import com.taskora.api.features.admin.service.AdminStatsService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminStatsService.getDashboardStats());
    }
}
