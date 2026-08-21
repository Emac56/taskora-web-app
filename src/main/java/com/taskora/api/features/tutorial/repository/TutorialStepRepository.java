package com.taskora.api.features.tutorial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskora.api.features.tutorial.entity.TutorialStep;

public interface TutorialStepRepository
        extends JpaRepository<TutorialStep, Long> {

    // Automatically derives SQL with "ORDER BY step_number ASC"
    List<TutorialStep> findAllByTutorialIdOrderByStepNumberAsc(Long tutorialId);

    // Used on CREATE: is stepNumber already taken by another step of this tutorial?
    boolean existsByTutorialIdAndStepNumber(Long tutorialId, Integer stepNumber);

    // Used on UPDATE: same check, but excludes the step being updated itself
    // (otherwise a step would collide with its own unchanged stepNumber).
    boolean existsByTutorialIdAndStepNumberAndIdNot(
            Long tutorialId, Integer stepNumber, Long id);
}
