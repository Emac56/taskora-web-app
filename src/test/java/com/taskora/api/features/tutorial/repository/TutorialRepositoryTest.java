package com.taskora.api.features.tutorial.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.entity.TutorialStep;
import com.taskora.api.features.tutorial.enums.TutorialStatus;

@DataJpaTest
class TutorialRepositoryTest {

    @Autowired
    private TutorialRepository tutorialRepository;

    @Autowired
    private TutorialStepRepository tutorialStepRepository;

    @Test
    void shouldSaveAndFindTutorial() {
        Tutorial tutorial = new Tutorial();

        tutorial.setTitle("Java Basics");
        tutorial.setDescription("Learn Java fundamentals.");
        tutorial.setStatus(TutorialStatus.DRAFT);

        Tutorial savedTutorial = tutorialRepository.save(tutorial);

        Optional<Tutorial> result =
                tutorialRepository.findById(savedTutorial.getId());

        assertTrue(result.isPresent());
        assertEquals("Java Basics", result.get().getTitle());
        assertEquals(
                "Learn Java fundamentals.",
                result.get().getDescription()
        );
        assertEquals(
                TutorialStatus.DRAFT,
                result.get().getStatus()
        );
    }

    @Test
    void shouldFindAllTutorials() {
        Tutorial firstTutorial = new Tutorial();
        firstTutorial.setTitle("Java Basics");
        firstTutorial.setDescription("Learn Java fundamentals.");
        firstTutorial.setStatus(TutorialStatus.DRAFT);

        Tutorial secondTutorial = new Tutorial();
        secondTutorial.setTitle("Spring Boot Basics");
        secondTutorial.setDescription("Learn Spring Boot fundamentals.");
        secondTutorial.setStatus(TutorialStatus.PUBLISHED);

        tutorialRepository.save(firstTutorial);
        tutorialRepository.save(secondTutorial);

        assertEquals(2, tutorialRepository.findAll().size());
    }

    @Test
    void shouldDeleteTutorial() {
        Tutorial tutorial = new Tutorial();

        tutorial.setTitle("Temporary Tutorial");
        tutorial.setDescription("Tutorial to be deleted.");
        tutorial.setStatus(TutorialStatus.DRAFT);

        Tutorial savedTutorial = tutorialRepository.save(tutorial);

        tutorialRepository.deleteById(savedTutorial.getId());

        assertFalse(
                tutorialRepository.existsById(savedTutorial.getId())
        );
    }
    
    @Test
    void shouldDeleteTutorialAndItsSteps() {
        Tutorial tutorial = new Tutorial();
        tutorial.setTitle("Tutorial With Steps");
        tutorial.setDescription("Has steps that must cascade delete.");
        tutorial.setStatus(TutorialStatus.DRAFT);

        TutorialStep step = new TutorialStep();
        step.setTutorial(tutorial);
        step.setStepNumber(1);
        step.setInstruction("First step.");

        tutorial.getTutorialStep().add(step);

        Tutorial savedTutorial = tutorialRepository.save(tutorial);
        Long tutorialId = savedTutorial.getId();

        tutorialRepository.deleteById(tutorialId);

        assertFalse(tutorialRepository.existsById(tutorialId));
        assertTrue(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(tutorialId).isEmpty());
    }
}
