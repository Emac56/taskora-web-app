package com.taskora.api.features.tutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskora.api.features.tutorial.entity.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
}