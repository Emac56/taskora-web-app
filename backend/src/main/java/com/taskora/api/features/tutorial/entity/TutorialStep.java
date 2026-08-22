package com.taskora.api.features.tutorial.entity;

import com.taskora.api.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "tutorial_steps",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_tutorial_steps_tutorial_id_step_number",
                columnNames = {"tutorial_id", "step_number"}))
public class TutorialStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutorial_id")
    private Tutorial tutorial;

    private Integer stepNumber;

    @Column(length = 5000)
    private String instruction;

    @Column(length = 2048)
    private String imageUrl;

}
