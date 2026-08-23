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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.taskora.api.common.exception.DuplicateStepNumberException;
import com.taskora.api.common.exception.ResourceNotFoundException;
import com.taskora.api.common.security.CurrentUserProvider;
import com.taskora.api.common.storage.SupabaseStorageClient;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.ReplaceTutorialStepItem;
import com.taskora.api.features.tutorial.dto.request.ReplaceTutorialStepRequest;
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

    private static final String TUTORIAL_NOT_FOUND_MESSAGE = "Tutorial not found.";
    private static final String TUTORIAL_STEP_NOT_FOUND_MESSAGE = "Tutorial step not found.";

    private final TutorialStepRepository tutorialStepRepository;
    private final TutorialRepository tutorialRepository;
    private final TutorialStepMapper tutorialStepMapper;
    private final CurrentUserProvider currentUserProvider;
    private final SupabaseStorageClient supabaseStorageClient;

    public TutorialStepServiceImpl(
            TutorialStepRepository tutorialStepRepository,
            TutorialRepository tutorialRepository,
            TutorialStepMapper tutorialStepMapper,
            CurrentUserProvider currentUserProvider,
            SupabaseStorageClient supabaseStorageClient) {

        this.tutorialStepRepository = tutorialStepRepository;
        this.tutorialRepository = tutorialRepository;
        this.tutorialStepMapper = tutorialStepMapper;
        this.currentUserProvider = currentUserProvider;
        this.supabaseStorageClient = supabaseStorageClient;
    }

    @Override
    public TutorialStepResponse create(
            Long tutorialId,
            CreateTutorialStepRequest request) {

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(TUTORIAL_NOT_FOUND_MESSAGE));

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
                                TUTORIAL_STEP_NOT_FOUND_MESSAGE));

        if (isDraftHiddenFromCaller(tutorialStep.getTutorial())) {
            throw new ResourceNotFoundException(TUTORIAL_STEP_NOT_FOUND_MESSAGE);
        }

        return tutorialStepMapper.toResponse(tutorialStep);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorialStepResponse> getAllByTutorialId(Long tutorialId) {

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(TUTORIAL_NOT_FOUND_MESSAGE));

        if (isDraftHiddenFromCaller(tutorial)) {
            throw new ResourceNotFoundException(TUTORIAL_NOT_FOUND_MESSAGE);
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
                                TUTORIAL_STEP_NOT_FOUND_MESSAGE));

        Long tutorialId = tutorialStep.getTutorial().getId();

        if (tutorialStepRepository.existsByTutorialIdAndStepNumberAndIdNot(
                tutorialId, request.getStepNumber(), id)) {
            throw new DuplicateStepNumberException(
                    "Step number " + request.getStepNumber()
                            + " already exists for this tutorial.");
        }

        // Captured before the mapper overwrites it, so we know afterwards
        // whether the old object in storage needs to be cleaned up.
        String previousImageUrl = tutorialStep.getImageUrl();

        tutorialStepMapper.updateEntity(request, tutorialStep);

        TutorialStep updatedTutorialStep =
                tutorialStepRepository.save(tutorialStep);

        // This method carries no @Transactional of its own, so save()
        // above already committed by the time we get here — safe to
        // delete the old object now that the new one is durable.
        if (previousImageUrl != null
                && !previousImageUrl.equals(updatedTutorialStep.getImageUrl())) {
            supabaseStorageClient.delete(previousImageUrl);
        }

        return tutorialStepMapper.toResponse(updatedTutorialStep);
    }

    @Override
    public void delete(Long id) {

        TutorialStep tutorialStep = tutorialStepRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                TUTORIAL_STEP_NOT_FOUND_MESSAGE));

        tutorialStepRepository.delete(tutorialStep);

        // The DB row is already gone by this point. delete() is a no-op
        // for a null imageUrl, so steps without an image are unaffected.
        supabaseStorageClient.delete(tutorialStep.getImageUrl());
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
            ReplaceTutorialStepRequest request) {

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(TUTORIAL_NOT_FOUND_MESSAGE));

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

        validateIncomingIdsBelongToTutorial(incoming, existingById);

        Set<Long> incomingIds = incoming.stream()
                .map(ReplaceTutorialStepItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Captured before deleteAllByIdInBatch: once those rows are gone we
        // can't ask them for their imageUrl anymore.
        List<TutorialStep> stepsToDelete = existingSteps.stream()
                .filter(step -> !incomingIds.contains(step.getId()))
                .toList();
        List<Long> idsToDelete = stepsToDelete.stream()
                .map(TutorialStep::getId)
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
        // Kept steps whose imageUrl is being replaced have their previous
        // value captured here, before the overwrite below.
        List<String> replacedImageUrls = new ArrayList<>();
        List<TutorialStep> toSave =
                buildStepsToSave(incoming, existingById, tutorial, replacedImageUrls);

        List<TutorialStep> saved = tutorialStepRepository.saveAll(toSave);

        List<String> imageUrlsToCleanUp = new ArrayList<>(replacedImageUrls);
        imageUrlsToCleanUp.addAll(
                stepsToDelete.stream()
                        .map(TutorialStep::getImageUrl)
                        .filter(Objects::nonNull)
                        .toList());
        cleanUpAfterCommit(imageUrlsToCleanUp);

        return saved.stream()
                .sorted(Comparator.comparing(TutorialStep::getStepNumber))
                .map(tutorialStepMapper::toResponse)
                .toList();
    }

    // Security: an id in the payload must belong to THIS tutorial —
    // never trust a client-supplied id blindly.
    private void validateIncomingIdsBelongToTutorial(
            List<ReplaceTutorialStepItem> incoming,
            Map<Long, TutorialStep> existingById) {

        for (ReplaceTutorialStepItem item : incoming) {
            if (item.getId() != null && !existingById.containsKey(item.getId())) {
                throw new ResourceNotFoundException(
                        "Step " + item.getId() + " does not belong to this tutorial.");
            }
        }
    }

    // Builds the final entities to persist: reuses kept steps (by id) and
    // creates new ones for items with no id. For kept steps whose imageUrl
    // is being replaced, the previous value is captured into
    // replacedImageUrls before it gets overwritten below.
    private List<TutorialStep> buildStepsToSave(
            List<ReplaceTutorialStepItem> incoming,
            Map<Long, TutorialStep> existingById,
            Tutorial tutorial,
            List<String> replacedImageUrls) {

        List<TutorialStep> toSave = new ArrayList<>();
        for (ReplaceTutorialStepItem item : incoming) {
            TutorialStep step = item.getId() != null
                    ? existingById.get(item.getId())
                    : new TutorialStep();

            if (item.getId() == null) {
                step.setTutorial(tutorial);
            } else {
                String previousImageUrl = step.getImageUrl();
                if (previousImageUrl != null
                        && !previousImageUrl.equals(item.getImageUrl())) {
                    replacedImageUrls.add(previousImageUrl);
                }
            }
            step.setStepNumber(item.getStepNumber());
            step.setInstruction(item.getInstruction());
            step.setImageUrl(item.getImageUrl());
            toSave.add(step);
        }
        return toSave;
    }

    /**
     * Defers Supabase Storage deletes until this method's surrounding
     * {@code @Transactional} boundary actually commits.
     *
     * <p>replaceAll() runs several more DB statements after the point
     * where these URLs are known (saveAll, then the response mapping).
     * Deleting from storage immediately would mean that if anything later
     * in the same transaction throws and the whole thing rolls back, the
     * database still references images we already told Supabase to
     * delete — trading one inconsistency (orphaned files) for a worse one
     * (broken image links). Running outside an active transaction — not
     * expected given the {@code @Transactional} above, but kept as a safe
     * default — just deletes immediately instead of silently dropping
     * the cleanup.
     */
    private void cleanUpAfterCommit(List<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            imageUrls.forEach(supabaseStorageClient::delete);
                        }
                    });
        } else {
            imageUrls.forEach(supabaseStorageClient::delete);
        }
    }

    private boolean isDraftHiddenFromCaller(Tutorial tutorial) {
        return tutorial.getStatus() == TutorialStatus.DRAFT
                && !currentUserProvider.isAdmin();
    }
}
