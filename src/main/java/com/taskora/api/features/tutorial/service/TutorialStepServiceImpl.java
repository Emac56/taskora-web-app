package com.taskora.api.features.tutorial.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.taskora.api.features.tutorial.dto.request.CreateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialStepRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialStepResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.entity.TutorialStep;
import com.taskora.api.features.tutorial.mapper.TutorialStepMapper;
import com.taskora.api.features.tutorial.repository.TutorialRepository;
import com.taskora.api.features.tutorial.repository.TutorialStepRepository;

@Service
public class TutorialStepServiceImpl implements TutorialStepService {

    private final TutorialStepRepository tutorialStepRepository;
    private final TutorialRepository tutorialRepository;
    private final TutorialStepMapper tutorialStepMapper;

    public TutorialStepServiceImpl(
            TutorialStepRepository tutorialStepRepository,
            TutorialRepository tutorialRepository,
            TutorialStepMapper tutorialStepMapper) {

        this.tutorialStepRepository = tutorialStepRepository;
        this.tutorialRepository = tutorialRepository;
        this.tutorialStepMapper = tutorialStepMapper;
    }

    @Override
    public TutorialStepResponse create(
            Long tutorialId,
            CreateTutorialStepRequest request) {

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tutorial not found."));

        TutorialStep tutorialStep =
                tutorialStepMapper.toEntity(request);

        tutorialStep.setTutorial(tutorial);

        TutorialStep savedTutorialStep =
                tutorialStepRepository.save(tutorialStep);

        return tutorialStepMapper.toResponse(savedTutorialStep);
    }

    @Override
    public TutorialStepResponse getById(Long id) {

        TutorialStep tutorialStep =
                tutorialStepRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Tutorial step not found."));

        return tutorialStepMapper.toResponse(tutorialStep);
    }
@Override
public List<TutorialStepResponse> getAllByTutorialId(Long tutorialId) {

    return tutorialStepRepository.findAllByTutorialId(tutorialId)
            .stream()
            .map(tutorialStepMapper::toResponse)
            .toList();
}

    @Override
    public TutorialStepResponse update(
            Long id,
            UpdateTutorialStepRequest request) {

        TutorialStep tutorialStep =
                tutorialStepRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Tutorial step not found."));

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
}
