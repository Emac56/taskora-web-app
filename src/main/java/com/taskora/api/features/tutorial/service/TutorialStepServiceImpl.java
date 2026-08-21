package com.taskora.api.features.tutorial.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskora.api.common.exception.DuplicateStepNumberException;
import com.taskora.api.common.exception.ResourceNotFoundException;
import com.taskora.api.common.security.CurrentUserProvider;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
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

        // BE bug fix: reject duplicate stepNumber within the same tutorial
        // before it ever reaches the database.
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

        // Fetch steps ordered in ascending sequence from database
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

        // BE bug fix: reject duplicate stepNumber within the same tutorial,
        // excluding this step itself (so keeping the same number is fine).
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

    private boolean isDraftHiddenFromCaller(Tutorial tutorial) {
        return tutorial.getStatus() == TutorialStatus.DRAFT
                && !currentUserProvider.isAdmin();
    }
}
