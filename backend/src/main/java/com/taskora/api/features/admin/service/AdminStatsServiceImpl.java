package com.taskora.api.features.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskora.api.features.admin.dto.response.AdminDashboardStatsResponse;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.repository.TutorialRepository;
import com.taskora.api.features.tutorial.repository.TutorialStepRepository;

@Service
public class AdminStatsServiceImpl implements AdminStatsService {

    private final TutorialRepository tutorialRepository;
    private final TutorialStepRepository tutorialStepRepository;

    public AdminStatsServiceImpl(
            TutorialRepository tutorialRepository,
            TutorialStepRepository tutorialStepRepository) {
        this.tutorialRepository = tutorialRepository;
        this.tutorialStepRepository = tutorialStepRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        AdminDashboardStatsResponse response = new AdminDashboardStatsResponse();

        response.setTotalTutorials(tutorialRepository.count());
        response.setPublishedCount(
                tutorialRepository.countByStatus(TutorialStatus.PUBLISHED));
        response.setDraftCount(
                tutorialRepository.countByStatus(TutorialStatus.DRAFT));
        response.setTotalSteps(tutorialStepRepository.count());

        return response;
    }
}
