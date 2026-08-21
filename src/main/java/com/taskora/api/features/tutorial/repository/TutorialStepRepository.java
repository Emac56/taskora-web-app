package com.taskora.api.features.tutorial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskora.api.features.tutorial.entity.TutorialStep;

public interface TutorialStepRepository
        extends JpaRepository<TutorialStep, Long> {

    // Automatically derives SQL with "ORDER BY step_number ASC"
    List<TutorialStep> findAllByTutorialIdOrderByStepNumberAsc(Long tutorialId);
}
