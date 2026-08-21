package com.taskora.api.features.tutorial.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskora.api.common.exception.ResourceNotFoundException;
import com.taskora.api.common.security.CurrentUserProvider;
import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.enums.TutorialStatus;
import com.taskora.api.features.tutorial.mapper.TutorialMapper;
import com.taskora.api.features.tutorial.repository.TutorialRepository;

@Service
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;
    private final TutorialMapper tutorialMapper;
    private final CurrentUserProvider currentUserProvider;

    public TutorialServiceImpl(
            TutorialRepository tutorialRepository,
            TutorialMapper tutorialMapper,
            CurrentUserProvider currentUserProvider) {
        this.tutorialRepository = tutorialRepository;
        this.tutorialMapper = tutorialMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public TutorialResponse create(CreateTutorialRequest request) {
        Tutorial tutorial = tutorialMapper.toEntity(request);
        Tutorial savedTutorial = tutorialRepository.save(tutorial);
        return tutorialMapper.toResponse(savedTutorial);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorialResponse getById(Long id) {
        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tutorial not found."));

        if (isDraftHiddenFromCaller(tutorial)) {
            throw new ResourceNotFoundException("Tutorial not found.");
        }

        return tutorialMapper.toResponse(tutorial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorialResponse> getAll() {
        boolean isAdmin = currentUserProvider.isAdmin();

        return tutorialRepository.findAll()
                .stream()
                .filter(tutorial -> isAdmin
                        || tutorial.getStatus() == TutorialStatus.PUBLISHED)
                .map(tutorialMapper::toResponse)
                .toList();
    }

    @Override
    public TutorialResponse update(
            Long id,
            UpdateTutorialRequest request) {

        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tutorial not found."));

        tutorialMapper.updateEntity(request, tutorial);

        Tutorial updatedTutorial = tutorialRepository.save(tutorial);

        return tutorialMapper.toResponse(updatedTutorial);
    }

    @Override
    public void delete(Long id) {
        if (!tutorialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tutorial not found.");
        }

        tutorialRepository.deleteById(id);
    }

    private boolean isDraftHiddenFromCaller(Tutorial tutorial) {
        return tutorial.getStatus() == TutorialStatus.DRAFT
                && !currentUserProvider.isAdmin();
    }
}