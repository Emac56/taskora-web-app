package com.taskora.api.features.admin.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.taskora.api.features.admin.dto.response.AdminDashboardStatsResponse;
import com.taskora.api.features.admin.service.AdminStatsService;

@WebMvcTest(AdminStatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminStatsService adminStatsService;

    @Test
    void shouldReturnDashboardStats() throws Exception {

        AdminDashboardStatsResponse response = new AdminDashboardStatsResponse();
        response.setTotalTutorials(5L);
        response.setPublishedCount(3L);
        response.setDraftCount(2L);
        response.setTotalSteps(21L);

        when(adminStatsService.getDashboardStats()).thenReturn(response);

        mockMvc.perform(
                get("/api/v1/admin/stats")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalTutorials").value(5))
        .andExpect(jsonPath("$.publishedCount").value(3))
        .andExpect(jsonPath("$.draftCount").value(2))
        .andExpect(jsonPath("$.totalSteps").value(21));

        verify(adminStatsService).getDashboardStats();
    }

    @Test
    void shouldReturnZeroedStatsForEmptyDataset() throws Exception {

        AdminDashboardStatsResponse response = new AdminDashboardStatsResponse();
        response.setTotalTutorials(0L);
        response.setPublishedCount(0L);
        response.setDraftCount(0L);
        response.setTotalSteps(0L);

        when(adminStatsService.getDashboardStats()).thenReturn(response);

        mockMvc.perform(
                get("/api/v1/admin/stats")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalTutorials").value(0))
        .andExpect(jsonPath("$.publishedCount").value(0))
        .andExpect(jsonPath("$.draftCount").value(0))
        .andExpect(jsonPath("$.totalSteps").value(0));

        verify(adminStatsService).getDashboardStats();
    }
}
