package com.taskora.api.features.tutorial.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.entity.TutorialStep;
import com.taskora.api.features.tutorial.enums.TutorialStatus;

@DataJpaTest
class TutorialStepRepositoryTest {

    @Autowired
    private TutorialStepRepository tutorialStepRepository;

    @Autowired
    private TutorialRepository tutorialRepository;

    private Tutorial persistTutorial() {
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle("Java Basics");
        tutorial.setDescription("Learn Java fundamentals.");
        tutorial.setStatus(TutorialStatus.DRAFT);
        return tutorialRepository.save(tutorial);
    }

    @Test
    void shouldSaveAndFindTutorialStep() {
        Tutorial savedTutorial = persistTutorial();

        TutorialStep step = new TutorialStep();
        step.setTutorial(savedTutorial);
        step.setStepNumber(1);
        step.setInstruction("Open the Java project.");

        TutorialStep savedStep = tutorialStepRepository.save(step);

        Optional<TutorialStep> result =
                tutorialStepRepository.findById(savedStep.getId());

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getStepNumber());
        assertEquals(
                "Open the Java project.",
                result.get().getInstruction()
        );
    }

    @Test
    void shouldFindAllTutorialStepsOrderedByStepNumberAsc() {
        Tutorial savedTutorial = persistTutorial();

        // Save Step 2 first into DB
        TutorialStep secondStep = new TutorialStep();
        secondStep.setTutorial(savedTutorial);
        secondStep.setStepNumber(2);
        secondStep.setInstruction("Create a Java class.");
        tutorialStepRepository.save(secondStep);

        // Save Step 1 second into DB
        TutorialStep firstStep = new TutorialStep();
        firstStep.setTutorial(savedTutorial);
        firstStep.setStepNumber(1);
        firstStep.setInstruction("Open the project.");
        tutorialStepRepository.save(firstStep);

        List<TutorialStep> steps =
                tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(savedTutorial.getId());

        assertEquals(2, steps.size());
        // Verify that steps are returned ordered by stepNumber ascending (Step 1 first, then Step 2)
        assertEquals(1, steps.get(0).getStepNumber());
        assertEquals(2, steps.get(1).getStepNumber());
    }

    @Test
    void shouldDeleteTutorialStep() {
        Tutorial savedTutorial = persistTutorial();

        TutorialStep step = new TutorialStep();
        step.setTutorial(savedTutorial);
        step.setStepNumber(1);
        step.setInstruction("Temporary step.");

        TutorialStep savedStep = tutorialStepRepository.save(step);

        tutorialStepRepository.deleteById(savedStep.getId());

        assertFalse(
                tutorialStepRepository.existsById(savedStep.getId())
        );
    }

    // ---------- NEW: duplicate stepNumber guard (BE bug fix) ----------

    @Test
    void existsByTutorialIdAndStepNumberShouldReturnTrueWhenTaken() {
        Tutorial savedTutorial = persistTutorial();

        TutorialStep step = new TutorialStep();
        step.setTutorial(savedTutorial);
        step.setStepNumber(1);
        step.setInstruction("Open the project.");
        tutorialStepRepository.save(step);

        boolean exists = tutorialStepRepository
                .existsByTutorialIdAndStepNumber(savedTutorial.getId(), 1);

        assertTrue(exists);
    }

    @Test
    void existsByTutorialIdAndStepNumberShouldReturnFalseWhenNotTaken() {
        Tutorial savedTutorial = persistTutorial();

        boolean exists = tutorialStepRepository
                .existsByTutorialIdAndStepNumber(savedTutorial.getId(), 1);

        assertFalse(exists);
    }

    @Test
    void existsByTutorialIdAndStepNumberShouldNotLeakAcrossDifferentTutorials() {
        Tutorial firstTutorial = persistTutorial();
        Tutorial secondTutorial = persistTutorial();

        TutorialStep step = new TutorialStep();
        step.setTutorial(firstTutorial);
        step.setStepNumber(1);
        step.setInstruction("Open the project.");
        tutorialStepRepository.save(step);

        // Same stepNumber but a different tutorial — must be allowed.
        boolean exists = tutorialStepRepository
                .existsByTutorialIdAndStepNumber(secondTutorial.getId(), 1);

        assertFalse(exists);
    }

    @Test
    void existsByTutorialIdAndStepNumberAndIdNotShouldExcludeTheStepItself() {
        Tutorial savedTutorial = persistTutorial();

        TutorialStep step = new TutorialStep();
        step.setTutorial(savedTutorial);
        step.setStepNumber(1);
        step.setInstruction("Open the project.");
        TutorialStep savedStep = tutorialStepRepository.save(step);

        // Checking the step against its own current stepNumber must not
        // count as a collision — otherwise no-op updates would fail.
        boolean exists = tutorialStepRepository
                .existsByTutorialIdAndStepNumberAndIdNot(
                        savedTutorial.getId(), 1, savedStep.getId());

        assertFalse(exists);
    }

    @Test
    void existsByTutorialIdAndStepNumberAndIdNotShouldDetectCollisionWithAnotherStep() {
        Tutorial savedTutorial = persistTutorial();

        TutorialStep firstStep = new TutorialStep();
        firstStep.setTutorial(savedTutorial);
        firstStep.setStepNumber(1);
        firstStep.setInstruction("Open the project.");
        tutorialStepRepository.save(firstStep);

        TutorialStep secondStep = new TutorialStep();
        secondStep.setTutorial(savedTutorial);
        secondStep.setStepNumber(2);
        secondStep.setInstruction("Create a Java class.");
        TutorialStep savedSecondStep = tutorialStepRepository.save(secondStep);

        // secondStep is trying to change into stepNumber 1, which firstStep
        // already owns — must be detected as a collision.
        boolean exists = tutorialStepRepository
                .existsByTutorialIdAndStepNumberAndIdNot(
                        savedTutorial.getId(), 1, savedSecondStep.getId());

        assertTrue(exists);
    }

    @Test
    void shouldEnforceUniqueTutorialIdAndStepNumberAtDatabaseLevel() {
        Tutorial savedTutorial = persistTutorial();

        TutorialStep firstStep = new TutorialStep();
        firstStep.setTutorial(savedTutorial);
        firstStep.setStepNumber(1);
        firstStep.setInstruction("Open the project.");
        tutorialStepRepository.saveAndFlush(firstStep);

        TutorialStep duplicateStep = new TutorialStep();
        duplicateStep.setTutorial(savedTutorial);
        duplicateStep.setStepNumber(1);
        duplicateStep.setInstruction("Duplicate step number.");

        // Last line of defense: even bypassing the service-layer check,
        // the DB constraint from V2__add_unique_step_number_per_tutorial.sql
        // (mirrored on the entity via @Table(uniqueConstraints = ...))
        // must reject this.
        assertThrows(
                DataIntegrityViolationException.class,
                () -> tutorialStepRepository.saveAndFlush(duplicateStep)
        );
    }

    // --------------------------------------------------------------------
}
