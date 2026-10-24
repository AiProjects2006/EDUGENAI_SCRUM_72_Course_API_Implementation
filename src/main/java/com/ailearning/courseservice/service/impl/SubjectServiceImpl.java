package com.ailearning.courseservice.service.impl;

import com.ailearning.courseservice.dto.request.CreateSubjectRequest;
import com.ailearning.courseservice.dto.request.UpdateSubjectRequest;
import com.ailearning.courseservice.dto.response.SubjectResponse;
import com.ailearning.courseservice.entity.Subject;
import com.ailearning.courseservice.exception.ResourceNotFoundException;
import com.ailearning.courseservice.repository.SubjectRepository;
import com.ailearning.courseservice.service.RedisCacheService;
import com.ailearning.courseservice.service.SubjectService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.course.ttl-seconds:86400}")
    private long cacheTtlSeconds;

    public SubjectServiceImpl(SubjectRepository subjectRepository,
                              RedisCacheService redisCacheService,
                              ObjectMapper objectMapper) {
        this.subjectRepository = subjectRepository;
        this.redisCacheService = redisCacheService;
        this.objectMapper = objectMapper;
    }

    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request) {

        Subject subject = Subject.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Subject savedSubject = subjectRepository.save(subject);
        SubjectResponse response = mapToResponse(savedSubject);

        evictSubjectCache(savedSubject.getSubjectId());

        return response;
    }

    @Override
    public List<SubjectResponse> getAllSubjects() {
        String cacheKey = "subject:all";
        String cached = redisCacheService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                log.info("Redis cache HIT for key [{}]", cacheKey);
                return objectMapper.readValue(cached, new TypeReference<List<SubjectResponse>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize cached subjects: {}", e.getMessage());
            }
        }

        log.info("Redis cache MISS for key [{}], reading from Course DB", cacheKey);
        List<SubjectResponse> subjects = subjectRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        try {
            redisCacheService.set(cacheKey, objectMapper.writeValueAsString(subjects), cacheTtlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache subjects in Redis: {}", e.getMessage());
        }

        return subjects;
    }

    @Override
    public SubjectResponse getSubjectById(Long subjectId) {
        String cacheKey = "subject:id:" + subjectId;
        String cached = redisCacheService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                log.info("Redis cache HIT for key [{}]", cacheKey);
                return objectMapper.readValue(cached, SubjectResponse.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize cached subject: {}", e.getMessage());
            }
        }

        log.info("Redis cache MISS for key [{}], reading from Course DB", cacheKey);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with id : " + subjectId));

        SubjectResponse response = mapToResponse(subject);

        try {
            redisCacheService.set(cacheKey, objectMapper.writeValueAsString(response), cacheTtlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache subject in Redis: {}", e.getMessage());
        }

        return response;
    }

    @Override
    public SubjectResponse updateSubject(Long subjectId,
            UpdateSubjectRequest request) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with id : " + subjectId));

        subject.setName(request.getName());
        subject.setDescription(request.getDescription());

        Subject updatedSubject = subjectRepository.save(subject);
        SubjectResponse response = mapToResponse(updatedSubject);

        evictSubjectCache(subjectId);

        return response;
    }

    @Override
    public void deleteSubject(Long subjectId) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with id : " + subjectId));

        subjectRepository.delete(subject);

        evictSubjectCache(subjectId);
    }

    private void evictSubjectCache(Long subjectId) {
        redisCacheService.delete("subject:all");
        if (subjectId != null) {
            redisCacheService.delete("subject:id:" + subjectId);
        }
    }

    private SubjectResponse mapToResponse(Subject subject) {

        return SubjectResponse.builder()
                .subjectId(subject.getSubjectId())
                .name(subject.getName())
                .description(subject.getDescription())
                .build();
    }
}