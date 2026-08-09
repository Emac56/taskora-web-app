package com.taskora.api.features.tutorial.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.taskora.api.features.tutorial.dto.request.CreateTutorialRequest;
import com.taskora.api.features.tutorial.dto.request.UpdateTutorialRequest;
import com.taskora.api.features.tutorial.dto.response.TutorialResponse;
import com.taskora.api.features.tutorial.entity.Tutorial;
import com.taskora.api.features.tutorial.mapper.TutorialMapper;
import com.taskora.api.features.tutorial.repository.TutorialRepository;

@Service
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;
    private final TutorialMapper tutorialMapper;

    public TutorialServiceImpl(
            TutorialRepository tutorialRepository,
            TutorialMapper tutorialMapper) {
        this.tutorialRepository = tutorialRepository;
        this.tutorialMapper = tutorialMapper;
    }

    @Override
    public TutorialResponse create(CreateTutorialRequest request) {
        Tutorial tutorial = tutorialMapper.toEntity(request);

        Tutorial savedTutorial = tutorialRepository.save(tutorial);

        return tutorialMapper.toResponse(savedTutorial);
    }

    @Override
    public TutorialResponse getById(Long id) {
        Tutorial tutorial = tutorialRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tutorial not found."));

        return tutorialMapper.toResponse(tutorial);
    }

    @Override
    public List<TutorialResponse> getAll() {
        return tutorialRepository.findAll()
                .stream()
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
}