package com.ailearning.courseservice.service.impl;

import com.ailearning.courseservice.dto.request.CreateSubjectRequest;
import com.ailearning.courseservice.dto.request.UpdateSubjectRequest;
import com.ailearning.courseservice.dto.response.SubjectResponse;
import com.ailearning.courseservice.entity.Subject;
import com.ailearning.courseservice.exception.ResourceNotFoundException;
import com.ailearning.courseservice.repository.SubjectRepository;
import com.ailearning.courseservice.service.SubjectService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request) {

        Subject subject = Subject.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Subject savedSubject = subjectRepository.save(subject);

        return mapToResponse(savedSubject);
    }

    @Override
    public List<SubjectResponse> getAllSubjects() {

        return subjectRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectResponse getSubjectById(Long subjectId) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found with id : " + subjectId));

        return mapToResponse(subject);
    }

    @Override
    public SubjectResponse updateSubject(Long subjectId,
                                         UpdateSubjectRequest request) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found with id : " + subjectId));

        subject.setName(request.getName());
        subject.setDescription(request.getDescription());

        Subject updatedSubject = subjectRepository.save(subject);

        return mapToResponse(updatedSubject);
    }

    @Override
    public void deleteSubject(Long subjectId) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject not found with id : " + subjectId));

        subjectRepository.delete(subject);
    }

    private SubjectResponse mapToResponse(Subject subject) {

        return SubjectResponse.builder()
                .subjectId(subject.getSubjectId())
                .name(subject.getName())
                .description(subject.getDescription())
                .build();
    }
}