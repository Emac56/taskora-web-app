package com.taskora.api.features.tutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.enums.TutorialStatus;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

    long countByStatus(TutorialStatus status);
}
