package com.taskora.api.features.tutorial.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.taskora.api.common.exception.DuplicateStepNumberException;
import com.taskora.api.common.exception.ResourceNotFoundException;
import com.taskora.api.common.security.CurrentUserProvider;
import com.taskora.api.common.storage.SupabaseStorageClient;   // ← BAGO
import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.ReplaceTutorialStepItem;      // ← BAGO
import com.taskora.api.features.tutorial.dto.request.ReplaceTutorialStepRequest;  // ← BAGO
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.entity.TutorialStep;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.mapper.TutorialStepMapper;
import com.taskora.api.features.tutorial.repository.TutorialRepository;
import com.taskora.api.features.tutorial.repository.TutorialStepRepository;

@ExtendWith(MockitoExtension.class)
class TutorialStepServiceImplTest {

    @Mock
    private TutorialStepRepository tutorialStepRepository;

    @Mock
    private TutorialRepository tutorialRepository;

    @Mock
    private TutorialStepMapper tutorialStepMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private SupabaseStorageClient supabaseStorageClient; // ← BAGO

    @InjectMocks
    private TutorialStepServiceImpl tutorialStepService;

    private Tutorial tutorial;
    private TutorialStep tutorialStep;
    private TutorialStepResponse response;
    private CreateTutorialStepRequest createRequest;
    private UpdateTutorialStepRequest updateRequest;

    @BeforeEach
    void setUp() {
        tutorial = new Tutorial();
        tutorial.setId(1L);
        tutorial.setTitle("Java Basics");
        tutorial.setStatus(TutorialStatus.PUBLISHED);

        tutorialStep = new TutorialStep();
        tutorialStep.setId(10L);
        tutorialStep.setTutorial(tutorial);
        tutorialStep.setStepNumber(1);
        tutorialStep.setInstruction("Open the project.");
        tutorialStep.setImageUrl("https://example.com/image.png");

        response = new TutorialStepResponse();
        response.setId(10L);
        response.setStepNumber(1);
        response.setInstruction("Open the project.");
        response.setImageUrl("https://example.com/image.png");

        createRequest = new CreateTutorialStepRequest();
        createRequest.setStepNumber(1);
        createRequest.setInstruction("Open the project.");

        updateRequest = new UpdateTutorialStepRequest();
        updateRequest.setStepNumber(2);
        updateRequest.setInstruction("Create a Java class.");
    }

    @Test
    void shouldCreateTutorialStep() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(tutorial));

        when(tutorialStepRepository.existsByTutorialIdAndStepNumber(1L, 1))
                .thenReturn(false);

        when(tutorialStepMapper.toEntity(createRequest))
                .thenReturn(tutorialStep);

        when(tutorialStepRepository.save(tutorialStep))
                .thenReturn(tutorialStep);

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        TutorialStepResponse result =
                tutorialStepService.create(1L, createRequest);

        assertEquals(response, result);

        verify(tutorialRepository).findById(1L);
        verify(tutorialStepRepository).existsByTutorialIdAndStepNumber(1L, 1);
        verify(tutorialStepMapper).toEntity(createRequest);
        verify(tutorialStepRepository).save(tutorialStep);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }

    @Test
    void shouldThrowExceptionWhenCreatingStepForNonExistingTutorial() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.create(1L, createRequest)
        );

        verify(tutorialRepository).findById(1L);
    }

    // ---------- NEW: duplicate stepNumber guard (BE bug fix) ----------

    @Test
    void shouldThrowDuplicateStepNumberExceptionWhenCreatingWithExistingStepNumber() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(tutorial));

        when(tutorialStepRepository.existsByTutorialIdAndStepNumber(1L, 1))
                .thenReturn(true);

        assertThrows(
                DuplicateStepNumberException.class,
                () -> tutorialStepService.create(1L, createRequest)
        );

        verify(tutorialRepository).findById(1L);
        verify(tutorialStepRepository).existsByTutorialIdAndStepNumber(1L, 1);
        // Must never reach save() once a duplicate is detected.
        verify(tutorialStepRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void shouldThrowDuplicateStepNumberExceptionWhenUpdatingToExistingStepNumber() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));

        when(tutorialStepRepository.existsByTutorialIdAndStepNumberAndIdNot(1L, 2, 10L))
                .thenReturn(true);

        assertThrows(
                DuplicateStepNumberException.class,
                () -> tutorialStepService.update(10L, updateRequest)
        );

        verify(tutorialStepRepository).findById(10L);
        verify(tutorialStepRepository)
                .existsByTutorialIdAndStepNumberAndIdNot(1L, 2, 10L);
        verify(tutorialStepRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void shouldAllowUpdateWhenStepNumberUnchanged() {
        // Keeping the same stepNumber must not collide with itself.
        UpdateTutorialStepRequest sameNumberRequest = new UpdateTutorialStepRequest();
        sameNumberRequest.setStepNumber(1);
        sameNumberRequest.setInstruction("Updated instruction.");

        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));

        when(tutorialStepRepository.existsByTutorialIdAndStepNumberAndIdNot(1L, 1, 10L))
                .thenReturn(false);

        when(tutorialStepRepository.save(tutorialStep))
                .thenReturn(tutorialStep);

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        TutorialStepResponse result =
                tutorialStepService.update(10L, sameNumberRequest);

        assertEquals(response, result);
        verify(tutorialStepRepository).save(tutorialStep);
    }

    // ---------- NEW: storage cleanup on update() (BE-151) ----------

    @Test
    void shouldDeletePreviousImageFromStorageWhenImageUrlChangesOnUpdate() {
        UpdateTutorialStepRequest requestWithNewImage = new UpdateTutorialStepRequest();
        requestWithNewImage.setStepNumber(1);
        requestWithNewImage.setInstruction("Updated instruction.");
        requestWithNewImage.setImageUrl("https://example.com/new-image.png");

        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));
        when(tutorialStepRepository.existsByTutorialIdAndStepNumberAndIdNot(1L, 1, 10L))
                .thenReturn(false);

        // tutorialStepMapper is a mock elsewhere in this class (a no-op on
        // updateEntity), so here we simulate what the real mapper does:
        // overwrite imageUrl on the entity being updated.
        doAnswer(invocation -> {
            tutorialStep.setImageUrl("https://example.com/new-image.png");
            return null;
        }).when(tutorialStepMapper).updateEntity(requestWithNewImage, tutorialStep);

        when(tutorialStepRepository.save(tutorialStep))
                .thenReturn(tutorialStep);
        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        tutorialStepService.update(10L, requestWithNewImage);

        verify(supabaseStorageClient).delete("https://example.com/image.png");
    }

    @Test
    void shouldNotDeleteFromStorageWhenImageUrlUnchangedOnUpdate() {
        // tutorialStepMapper.updateEntity() is unstubbed here (no-op), so
        // tutorialStep.getImageUrl() is identical before and after — the
        // exact "nothing actually changed" case the diff-check must skip.
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));
        when(tutorialStepRepository.existsByTutorialIdAndStepNumberAndIdNot(1L, 2, 10L))
                .thenReturn(false);
        when(tutorialStepRepository.save(tutorialStep))
                .thenReturn(tutorialStep);
        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        tutorialStepService.update(10L, updateRequest);

        verify(supabaseStorageClient, never()).delete(ArgumentMatchers.any());
    }

    @Test
    void shouldNotDeleteFromStorageWhenUpdatedStepHadNoPreviousImage() {
        // previousImageUrl == null must short-circuit the && before ever
        // reaching .equals() — SonarCloud flagged this branch as uncovered
        // since every other update() test here starts from a step that
        // already has an image.
        TutorialStep stepWithoutImage = new TutorialStep();
        stepWithoutImage.setId(30L);
        stepWithoutImage.setTutorial(tutorial);
        stepWithoutImage.setStepNumber(1);
        stepWithoutImage.setImageUrl(null);

        when(tutorialStepRepository.findById(30L))
                .thenReturn(Optional.of(stepWithoutImage));
        when(tutorialStepRepository.existsByTutorialIdAndStepNumberAndIdNot(1L, 2, 30L))
                .thenReturn(false);
        when(tutorialStepRepository.save(stepWithoutImage))
                .thenReturn(stepWithoutImage);
        when(tutorialStepMapper.toResponse(stepWithoutImage))
                .thenReturn(response);

        tutorialStepService.update(30L, updateRequest);

        verify(supabaseStorageClient, never()).delete(ArgumentMatchers.any());
    }

    // --------------------------------------------------------------------

    @Test
    void shouldGetTutorialStepById() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        TutorialStepResponse result =
                tutorialStepService.getById(10L);

        assertEquals(response, result);

        verify(tutorialStepRepository).findById(10L);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }

    @Test
    void shouldThrowExceptionWhenTutorialStepNotFound() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.getById(10L)
        );

        verify(tutorialStepRepository).findById(10L);
    }

    @Test
    void shouldThrowNotFoundWhenNonAdminRequestsStepOfDraftTutorial() {
        Tutorial draftTutorial = new Tutorial();
        draftTutorial.setId(1L);
        draftTutorial.setStatus(TutorialStatus.DRAFT);

        TutorialStep draftStep = new TutorialStep();
        draftStep.setId(10L);
        draftStep.setTutorial(draftTutorial);

        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(draftStep));
        when(currentUserProvider.isAdmin()).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.getById(10L)
        );
    }

    @Test
    void shouldGetAllTutorialStepsByTutorialId() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(tutorial));

        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(tutorialStep));

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        List<TutorialStepResponse> result =
                tutorialStepService.getAllByTutorialId(1L);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(tutorialRepository).findById(1L);
        verify(tutorialStepRepository).findAllByTutorialIdOrderByStepNumberAsc(1L);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }

    @Test
    void shouldThrowNotFoundWhenNonAdminRequestsStepsOfDraftTutorial() {
        Tutorial draftTutorial = new Tutorial();
        draftTutorial.setId(1L);
        draftTutorial.setStatus(TutorialStatus.DRAFT);

        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.of(draftTutorial));
        when(currentUserProvider.isAdmin()).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.getAllByTutorialId(1L)
        );
    }

    // ---------- NEW: replaceAll (bulk atomic reorder) ----------

    @Test
    void shouldReorderTwoExistingStepsWithoutConflict() {
        TutorialStep stepA = new TutorialStep();
        stepA.setId(10L); stepA.setTutorial(tutorial); stepA.setStepNumber(1);
        TutorialStep stepB = new TutorialStep();
        stepB.setId(11L); stepB.setTutorial(tutorial); stepB.setStepNumber(2);

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(stepA, stepB));
        when(tutorialStepRepository.saveAllAndFlush(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepRepository.saveAll(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepMapper.toResponse(ArgumentMatchers.any())).thenReturn(response);

        ReplaceTutorialStepItem itemA = new ReplaceTutorialStepItem();
        itemA.setId(10L); itemA.setStepNumber(2); itemA.setInstruction("B");
        ReplaceTutorialStepItem itemB = new ReplaceTutorialStepItem();
        itemB.setId(11L); itemB.setStepNumber(1); itemB.setInstruction("A");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(itemA, itemB));

        // Must not throw — this is the exact swap that used to hit the
        // "conflicts with existing data" error under the old N-request flow.
        tutorialStepService.replaceAll(1L, req);

        verify(tutorialStepRepository).saveAllAndFlush(ArgumentMatchers.anyList());
        verify(tutorialStepRepository, never()).deleteAllByIdInBatch(ArgumentMatchers.anyList());
    }

    @Test
    void shouldCreateNewStepWhenPayloadItemHasNoId() {
        // One existing step is kept as-is; the second payload item has no id,
        // so it must go through the "create new step" branch (item.getId() ==
        // null -> new TutorialStep() + setTutorial(tutorial)) — the exact
        // branch SonarCloud flagged as uncovered.
        TutorialStep stepA = new TutorialStep();
        stepA.setId(10L); stepA.setTutorial(tutorial); stepA.setStepNumber(1);

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(stepA));
        when(tutorialStepRepository.saveAllAndFlush(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepRepository.saveAll(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepMapper.toResponse(ArgumentMatchers.any())).thenReturn(response);

        ReplaceTutorialStepItem itemA = new ReplaceTutorialStepItem();
        itemA.setId(10L); itemA.setStepNumber(1); itemA.setInstruction("A");

        ReplaceTutorialStepItem itemNew = new ReplaceTutorialStepItem();
        itemNew.setId(null);
        itemNew.setStepNumber(2);
        itemNew.setInstruction("New step");
        itemNew.setImageUrl("https://example.com/new.png");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(itemA, itemNew));

        tutorialStepService.replaceAll(1L, req);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TutorialStep>> captor = ArgumentCaptor.forClass(List.class);
        verify(tutorialStepRepository).saveAll(captor.capture());

        TutorialStep created = captor.getValue().stream()
                .filter(step -> step.getId() == null)
                .findFirst()
                .orElseThrow();

        assertEquals(tutorial, created.getTutorial());
        assertEquals(2, created.getStepNumber());
        assertEquals("New step", created.getInstruction());
        assertEquals("https://example.com/new.png", created.getImageUrl());
    }

    @Test
    void shouldDeleteStepsOmittedFromThePayload() {
        TutorialStep stepA = new TutorialStep();
        stepA.setId(10L); stepA.setTutorial(tutorial); stepA.setStepNumber(1);
        TutorialStep stepB = new TutorialStep();
        stepB.setId(11L); stepB.setTutorial(tutorial); stepB.setStepNumber(2);

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(stepA, stepB));
        when(tutorialStepRepository.saveAllAndFlush(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepRepository.saveAll(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepMapper.toResponse(ArgumentMatchers.any())).thenReturn(response);

        // Only step A survives in the payload; step B should be deleted.
        ReplaceTutorialStepItem itemA = new ReplaceTutorialStepItem();
        itemA.setId(10L); itemA.setStepNumber(1); itemA.setInstruction("A");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(itemA));

        tutorialStepService.replaceAll(1L, req);

        verify(tutorialStepRepository).deleteAllByIdInBatch(List.of(11L));
    }

    // ---------- NEW: storage cleanup on replaceAll() (BE-151) ----------
    // The ticket only named delete()/update(), but replaceAll() overwrites
    // and drops imageUrls the exact same way and is the endpoint the
    // frontend's bulk step editor actually calls — so it leaks the same
    // orphaned files if left alone.

    @Test
    void shouldDeleteRemovedStepImageFromStorageWhenOmittedFromPayload() {
        TutorialStep stepA = new TutorialStep();
        stepA.setId(10L); stepA.setTutorial(tutorial); stepA.setStepNumber(1);
        TutorialStep stepB = new TutorialStep();
        stepB.setId(11L); stepB.setTutorial(tutorial); stepB.setStepNumber(2);
        stepB.setImageUrl("https://example.com/step-b.png");

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(stepA, stepB));
        when(tutorialStepRepository.saveAllAndFlush(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepRepository.saveAll(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepMapper.toResponse(ArgumentMatchers.any())).thenReturn(response);

        ReplaceTutorialStepItem itemA = new ReplaceTutorialStepItem();
        itemA.setId(10L); itemA.setStepNumber(1); itemA.setInstruction("A");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(itemA));

        tutorialStepService.replaceAll(1L, req);

        verify(tutorialStepRepository).deleteAllByIdInBatch(List.of(11L));
        verify(supabaseStorageClient).delete("https://example.com/step-b.png");
    }

    @Test
    void shouldDeletePreviousImageFromStorageWhenKeptStepImageUrlChanges() {
        TutorialStep stepA = new TutorialStep();
        stepA.setId(10L); stepA.setTutorial(tutorial); stepA.setStepNumber(1);
        stepA.setImageUrl("https://example.com/old.png");

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(stepA));
        when(tutorialStepRepository.saveAllAndFlush(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepRepository.saveAll(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepMapper.toResponse(ArgumentMatchers.any())).thenReturn(response);

        ReplaceTutorialStepItem itemA = new ReplaceTutorialStepItem();
        itemA.setId(10L);
        itemA.setStepNumber(1);
        itemA.setInstruction("A");
        itemA.setImageUrl("https://example.com/new.png");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(itemA));

        tutorialStepService.replaceAll(1L, req);

        verify(supabaseStorageClient).delete("https://example.com/old.png");
    }

    @Test
    void shouldNotDeleteFromStorageWhenKeptStepImageUrlUnchanged() {
        TutorialStep stepA = new TutorialStep();
        stepA.setId(10L); stepA.setTutorial(tutorial); stepA.setStepNumber(1);
        stepA.setImageUrl("https://example.com/same.png");

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(stepA));
        when(tutorialStepRepository.saveAllAndFlush(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepRepository.saveAll(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepMapper.toResponse(ArgumentMatchers.any())).thenReturn(response);

        ReplaceTutorialStepItem itemA = new ReplaceTutorialStepItem();
        itemA.setId(10L);
        itemA.setStepNumber(1);
        itemA.setInstruction("A");
        itemA.setImageUrl("https://example.com/same.png");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(itemA));

        tutorialStepService.replaceAll(1L, req);

        verify(supabaseStorageClient, never()).delete(ArgumentMatchers.any());
    }

    @Test
    void shouldDeferStorageDeleteUntilAfterCommitWhenSynchronizationIsActive() {
        // Every other replaceAll() storage test here runs outside a real
        // Spring transaction, so isSynchronizationActive() is always false
        // and only the else-branch (immediate delete) ever runs.
        // SonarCloud flagged the true-branch — registerSynchronization()
        // plus its afterCommit() callback — as uncovered. This simulates
        // an actual active transaction to exercise it.
        TutorialStep stepA = new TutorialStep();
        stepA.setId(10L); stepA.setTutorial(tutorial); stepA.setStepNumber(1);
        stepA.setImageUrl("https://example.com/old.png");

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of(stepA));
        when(tutorialStepRepository.saveAllAndFlush(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepRepository.saveAll(ArgumentMatchers.anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tutorialStepMapper.toResponse(ArgumentMatchers.any())).thenReturn(response);

        ReplaceTutorialStepItem itemA = new ReplaceTutorialStepItem();
        itemA.setId(10L);
        itemA.setStepNumber(1);
        itemA.setInstruction("A");
        itemA.setImageUrl("https://example.com/new.png");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(itemA));

        TransactionSynchronizationManager.initSynchronization();
        try {
            tutorialStepService.replaceAll(1L, req);

            // Deferred: the delete must not fire while still "inside" the
            // transaction — only once afterCommit() runs.
            verify(supabaseStorageClient, never()).delete(ArgumentMatchers.any());

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(supabaseStorageClient).delete("https://example.com/old.png");
    }

    // --------------------------------------------------------------------

    @Test
    void shouldRejectDuplicateStepNumbersInPayload() {
        ReplaceTutorialStepItem itemA = new ReplaceTutorialStepItem();
        itemA.setId(10L); itemA.setStepNumber(1); itemA.setInstruction("A");
        ReplaceTutorialStepItem itemB = new ReplaceTutorialStepItem();
        itemB.setId(11L); itemB.setStepNumber(1); itemB.setInstruction("B");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(itemA, itemB));

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));

        assertThrows(DuplicateStepNumberException.class,
                () -> tutorialStepService.replaceAll(1L, req));

        verify(tutorialStepRepository, never())
                .findAllByTutorialIdOrderByStepNumberAsc(ArgumentMatchers.anyLong());
    }

    @Test
    void shouldRejectStepIdThatDoesNotBelongToTutorial() {
        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialStepRepository.findAllByTutorialIdOrderByStepNumberAsc(1L))
                .thenReturn(List.of());

        ReplaceTutorialStepItem foreignItem = new ReplaceTutorialStepItem();
        foreignItem.setId(999L); foreignItem.setStepNumber(1); foreignItem.setInstruction("Hijack");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(foreignItem));

        assertThrows(ResourceNotFoundException.class,
                () -> tutorialStepService.replaceAll(1L, req));
    }

    @Test
    void shouldThrowWhenReplacingStepsForNonExistingTutorial() {
        when(tutorialRepository.findById(1L)).thenReturn(Optional.empty());

        ReplaceTutorialStepItem item = new ReplaceTutorialStepItem();
        item.setStepNumber(1); item.setInstruction("A");

        ReplaceTutorialStepRequest req = new ReplaceTutorialStepRequest();
        req.setSteps(List.of(item));

        assertThrows(ResourceNotFoundException.class,
                () -> tutorialStepService.replaceAll(1L, req));
    }

    @Test
    void shouldUpdateTutorialStep() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));

        when(tutorialStepRepository.existsByTutorialIdAndStepNumberAndIdNot(1L, 2, 10L))
                .thenReturn(false);

        when(tutorialStepRepository.save(tutorialStep))
                .thenReturn(tutorialStep);

        when(tutorialStepMapper.toResponse(tutorialStep))
                .thenReturn(response);

        TutorialStepResponse result =
                tutorialStepService.update(10L, updateRequest);

        assertEquals(response, result);

        verify(tutorialStepRepository).findById(10L);
        verify(tutorialStepRepository)
                .existsByTutorialIdAndStepNumberAndIdNot(1L, 2, 10L);
        verify(tutorialStepMapper)
                .updateEntity(updateRequest, tutorialStep);
        verify(tutorialStepRepository).save(tutorialStep);
        verify(tutorialStepMapper).toResponse(tutorialStep);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTutorialStep() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.update(10L, updateRequest)
        );

        verify(tutorialStepRepository).findById(10L);
    }

    // ---------- delete() — updated for BE-151 (now loads the entity to
    // read its imageUrl before removing the row, instead of existsById) ----------

    @Test
    void shouldDeleteTutorialStep() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(tutorialStep));

        tutorialStepService.delete(10L);

        verify(tutorialStepRepository).findById(10L);
        verify(tutorialStepRepository).delete(tutorialStep);
        verify(supabaseStorageClient).delete(tutorialStep.getImageUrl());
    }

    @Test
    void shouldCallStorageDeleteWithNullWhenDeletedStepHasNoImage() {
        TutorialStep stepWithoutImage = new TutorialStep();
        stepWithoutImage.setId(20L);
        stepWithoutImage.setTutorial(tutorial);
        stepWithoutImage.setStepNumber(3);

        when(tutorialStepRepository.findById(20L))
                .thenReturn(Optional.of(stepWithoutImage));

        tutorialStepService.delete(20L);

        verify(tutorialStepRepository).delete(stepWithoutImage);
        // SupabaseStorageClient.delete() itself no-ops on null; the service
        // doesn't need its own null-check to get that safety.
        verify(supabaseStorageClient).delete(null);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTutorialStep() {
        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.delete(10L)
        );

        verify(tutorialStepRepository).findById(10L);
        verify(tutorialStepRepository, never()).delete(ArgumentMatchers.any());
        verify(supabaseStorageClient, never()).delete(ArgumentMatchers.any());
    }

    @Test
    void shouldThrowExceptionWhenTutorialNotFoundForGetAllByTutorialId() {
        when(tutorialRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tutorialStepService.getAllByTutorialId(1L)
        );

        verify(tutorialRepository).findById(1L);
    }

    @Test
    void shouldReturnTutorialStepWhenCallerIsAdminAndTutorialIsDraft() {
        Tutorial draftTutorial = new Tutorial();
        draftTutorial.setId(1L);
        draftTutorial.setStatus(TutorialStatus.DRAFT);

        TutorialStep draftStep = new TutorialStep();
        draftStep.setId(10L);
        draftStep.setTutorial(draftTutorial);

        when(tutorialStepRepository.findById(10L))
                .thenReturn(Optional.of(draftStep));
        when(currentUserProvider.isAdmin()).thenReturn(true);
        when(tutorialStepMapper.toResponse(draftStep))
                .thenReturn(response);

        TutorialStepResponse result = tutorialStepService.getById(10L);

        assertEquals(response, result);

        verify(currentUserProvider).isAdmin();
    }
}
