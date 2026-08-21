package com.taskora.api.features.tutorial.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.entity.TutorialStep;
import com.taskora.api.features.tutorial.enums.TutorialStatus;

@DataJpaTest
class TutorialStepRepositoryTest {

    @Autowired
    private TutorialStepRepository tutorialStepRepository;

    @Autowired
    private TutorialRepository tutorialRepository;

    @Test
    void shouldSaveAndFindTutorialStep() {
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle("Java Basics");
        tutorial.setDescription("Learn Java fundamentals.");
        tutorial.setStatus(TutorialStatus.DRAFT);

        Tutorial savedTutorial = tutorialRepository.save(tutorial);

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
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle("Java Basics");
        tutorial.setDescription("Learn Java fundamentals.");
        tutorial.setStatus(TutorialStatus.DRAFT);

        Tutorial savedTutorial = tutorialRepository.save(tutorial);

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
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle("Temporary Tutorial");
        tutorial.setDescription("Temporary tutorial.");
        tutorial.setStatus(TutorialStatus.DRAFT);

        Tutorial savedTutorial = tutorialRepository.save(tutorial);

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
}
