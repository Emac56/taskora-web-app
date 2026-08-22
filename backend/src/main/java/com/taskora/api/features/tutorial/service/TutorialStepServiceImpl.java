package com.taskora.api.features.tutorial.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskora.api.common.exception.DuplicateStepNumberException;
import com.taskora.api.common.exception.ResourceNotFoundException;
import com.taskora.api.common.security.CurrentUserProvider;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.ReplaceTutorialStepItem;
import com.taskora.api.features.tutorial.dto.request.ReplaceTutorialStepsRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.entity.TutorialStep;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.mapper.TutorialStepMapper;
import com.taskora.api.features.tutorial.repository.TutorialRepository;
import com.taskora.api.features.tutorial.repository.TutorialStepRepository;

@Service
public class TutorialStepServiceImpl implements TutorialStepService {

    private final TutorialStepRepository tutorialStepRepository;
    private final TutorialRepository tutorialRepository;
    private final TutorialStepMapper tutorialStepMapper;
    private final CurrentUserProvider currentUserProvider;

    public TutorialStepServiceImpl(
            TutorialStepRepository tutorialStepRepository,
            TutorialRepository tutorialRepository,
            TutorialStepMapper tutorialStepMapper,
            CurrentUserProvider currentUserProvider) {

        this.tutorialStepRepository = tutorialStepRepository;
        this.tutorialRepository = tutorialRepository;
        this.tutorialStepMapper = tutorialStepMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public TutorialStepResponse create(
            Long tutorialId,
            CreateTutorialStepRequest request) {

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tutorial not found."));

        if (tutorialStepRepository.existsByTutorialIdAndStepNumber(
                tutorialId, request.getStepNumber())) {
            throw new DuplicateStepNumberException(
                    "Step number " + request.getStepNumber()
                            + " already exists for this tutorial.");
        }

        TutorialStep tutorialStep = tutorialStepMapper.toEntity(request);
        tutorialStep.setTutorial(tutorial);

        TutorialStep savedTutorialStep =
                tutorialStepRepository.save(tutorialStep);

        return tutorialStepMapper.toResponse(savedTutorialStep);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorialStepResponse getById(Long id) {

        TutorialStep tutorialStep = tutorialStepRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tutorial step not found."));

        if (isDraftHiddenFromCaller(tutorialStep.getTutorial())) {
            throw new ResourceNotFoundException("Tutorial step not found.");
        }

        return tutorialStepMapper.toResponse(tutorialStep);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorialStepResponse> getAllByTutorialId(Long tutorialId) {

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tutorial not found."));

        if (isDraftHiddenFromCaller(tutorial)) {
            throw new ResourceNotFoundException("Tutorial not found.");
        }

        return tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(tutorialId)
                .stream()
                .map(tutorialStepMapper::toResponse)
                .toList();
    }

    @Override
    public TutorialStepResponse update(
            Long id,
            UpdateTutorialStepRequest request) {

        TutorialStep tutorialStep = tutorialStepRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tutorial step not found."));

        Long tutorialId = tutorialStep.getTutorial().getId();

        if (tutorialStepRepository.existsByTutorialIdAndStepNumberAndIdNot(
                tutorialId, request.getStepNumber(), id)) {
            throw new DuplicateStepNumberException(
                    "Step number " + request.getStepNumber()
                            + " already exists for this tutorial.");
        }

        tutorialStepMapper.updateEntity(request, tutorialStep);

        TutorialStep updatedTutorialStep =
                tutorialStepRepository.save(tutorialStep);

        return tutorialStepMapper.toResponse(updatedTutorialStep);
    }

    @Override
    public void delete(Long id) {

        if (!tutorialStepRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Tutorial step not found.");
        }

        tutorialStepRepository.deleteById(id);
    }

    // NEW: atomic bulk replace. Single transaction — create, update, delete,
    // and reorder all happen together or not at all. Fixes the root cause of
    // "This operation conflicts with existing data": the old flow was N
    // separate HTTP requests (one per step), so a mid-sequence failure left
    // the tutorial's steps half-renumbered, and the next save attempt would
    // collide against that stale state.
    @Override
    @Transactional
    public List<TutorialStepResponse> replaceAll(
            Long tutorialId,
            ReplaceTutorialStepsRequest request) {

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tutorial not found."));

        List<ReplaceTutorialStepItem> incoming = request.getSteps();

        // Fail fast: reject duplicate stepNumbers in the payload itself,
        // before touching the database.
        long distinctStepNumbers = incoming.stream()
                .map(ReplaceTutorialStepItem::getStepNumber)
                .distinct()
                .count();
        if (distinctStepNumbers != incoming.size()) {
            throw new DuplicateStepNumberException(
                    "Step numbers must be unique within a tutorial.");
        }

        List<TutorialStep> existingSteps =
                tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(tutorialId);
        Map<Long, TutorialStep> existingById = existingSteps.stream()
                .collect(Collectors.toMap(TutorialStep::getId, Function.identity()));

        // Security: an id in the payload must belong to THIS tutorial —
        // never trust a client-supplied id blindly.
        for (ReplaceTutorialStepItem item : incoming) {
            if (item.getId() != null && !existingById.containsKey(item.getId())) {
                throw new ResourceNotFoundException(
                        "Step " + item.getId() + " does not belong to this tutorial.");
            }
        }

        Set<Long> incomingIds = incoming.stream()
                .map(ReplaceTutorialStepItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Long> idsToDelete = existingSteps.stream()
                .map(TutorialStep::getId)
                .filter(id -> !incomingIds.contains(id))
                .toList();

        if (!idsToDelete.isEmpty()) {
            tutorialStepRepository.deleteAllByIdInBatch(idsToDelete);
        }

        // Phase 1: park every kept step at a guaranteed-unique negative
        // stepNumber and flush. Postgres checks unique constraints per
        // statement, not deferred — without this, swapping two existing
        // steps' numbers would collide mid-transaction even though the
        // final state is valid.
        List<TutorialStep> kept = incoming.stream()
                .filter(item -> item.getId() != null)
                .map(item -> existingById.get(item.getId()))
                .toList();

        for (int i = 0; i < kept.size(); i++) {
            kept.get(i).setStepNumber(-(i + 1));
        }
        tutorialStepRepository.saveAllAndFlush(kept);

        // Phase 2: write real final numbers; create brand-new steps.
        List<TutorialStep> toSave = new ArrayList<>();
        for (ReplaceTutorialStepItem item : incoming) {
            TutorialStep step = item.getId() != null
                    ? existingById.get(item.getId())
                    : new TutorialStep();

            if (item.getId() == null) {
                step.setTutorial(tutorial);
            }
            step.setStepNumber(item.getStepNumber());
            step.setInstruction(item.getInstruction());
            step.setImageUrl(item.getImageUrl());
            toSave.add(step);
        }

        List<TutorialStep> saved = tutorialStepRepository.saveAll(toSave);

        return saved.stream()
                .sorted(Comparator.comparing(TutorialStep::getStepNumber))
                .map(tutorialStepMapper::toResponse)
                .toList();
    }

    private boolean isDraftHiddenFromCaller(Tutorial tutorial) {
        return tutorial.getStatus() == TutorialStatus.DRAFT
                && !currentUserProvider.isAdmin();
    }
}
