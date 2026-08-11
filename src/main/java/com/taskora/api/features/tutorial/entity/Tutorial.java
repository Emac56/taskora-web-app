package com.taskora.api.features.tutorial.entity;

import com.taskora.api.common.entity.BaseEntity;
import com.taskora.api.features.tutorial.enums.TutorialStatus;

import jakarta.persistence.CascadeType;   // ← bagong import
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tutorials")
public class Tutorial extends BaseEntity {

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TutorialStatus status;

    @OneToMany(
        mappedBy = "tutorial",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<TutorialStep> tutorialStep = new ArrayList<>();
}