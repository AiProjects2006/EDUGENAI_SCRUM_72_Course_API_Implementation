package com.ailearning.courseservice.service.impl;

import com.ailearning.courseservice.dto.request.CreateCourseRequest;
import com.ailearning.courseservice.dto.request.UpdateCourseRequest;
import com.ailearning.courseservice.dto.response.CourseResponse;
import com.ailearning.courseservice.entity.Course;
import com.ailearning.courseservice.entity.Subject;
import com.ailearning.courseservice.exception.ResourceNotFoundException;
import com.ailearning.courseservice.repository.CourseRepository;
import com.ailearning.courseservice.repository.SubjectRepository;
import com.ailearning.courseservice.service.CourseService;
import com.ailearning.courseservice.service.RedisCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.course.ttl-seconds:86400}")
    private long cacheTtlSeconds;

    public CourseServiceImpl(CourseRepository courseRepository,
                             SubjectRepository subjectRepository,
                             RedisCacheService redisCacheService,
                             ObjectMapper objectMapper) {

        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
        this.redisCacheService = redisCacheService;
        this.objectMapper = objectMapper;
    }

    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found"));

        Course course = Course.builder()
                .subject(subject)
                .creatorId(request.getCreatorId())
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .level(request.getLevel())
                .status(request.getStatus())
                .build();

        Course savedCourse = courseRepository.save(course);
        CourseResponse response = mapToResponse(savedCourse);

        // Evict cached lists
        evictCourseCache(savedCourse.getCourseId(), subject.getSubjectId());

        return response;
    }

    @Override
    public List<CourseResponse> getAllCourses() {
        String cacheKey = "course:all";
        String cached = redisCacheService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                log.info("Redis cache HIT for key [{}]", cacheKey);
                return objectMapper.readValue(cached, new TypeReference<List<CourseResponse>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize cached courses: {}", e.getMessage());
            }
        }

        log.info("Redis cache MISS for key [{}], reading from Course DB", cacheKey);
        List<CourseResponse> courses = courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        try {
            redisCacheService.set(cacheKey, objectMapper.writeValueAsString(courses), cacheTtlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache courses in Redis: {}", e.getMessage());
        }

        return courses;
    }

    @Override
    public CourseResponse getCourseById(Long courseId) {
        String cacheKey = "course:id:" + courseId;
        String cached = redisCacheService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                log.info("Redis cache HIT for key [{}]", cacheKey);
                return objectMapper.readValue(cached, CourseResponse.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize cached course: {}", e.getMessage());
            }
        }

        log.info("Redis cache MISS for key [{}], reading from Course DB", cacheKey);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found"));

        CourseResponse response = mapToResponse(course);

        try {
            redisCacheService.set(cacheKey, objectMapper.writeValueAsString(response), cacheTtlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache course in Redis: {}", e.getMessage());
        }

        return response;
    }

    @Override
    public List<CourseResponse> getCoursesBySubject(Long subjectId) {
        String cacheKey = "course:subject:" + subjectId;
        String cached = redisCacheService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                log.info("Redis cache HIT for key [{}]", cacheKey);
                return objectMapper.readValue(cached, new TypeReference<List<CourseResponse>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize cached courses by subject: {}", e.getMessage());
            }
        }

        log.info("Redis cache MISS for key [{}], reading from Course DB", cacheKey);
        List<CourseResponse> courses = courseRepository.findBySubjectSubjectId(subjectId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        try {
            redisCacheService.set(cacheKey, objectMapper.writeValueAsString(courses), cacheTtlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache courses by subject in Redis: {}", e.getMessage());
        }

        return courses;
    }

    @Override
    public List<CourseResponse> getCoursesByCreator(Long creatorId) {
        String cacheKey = "course:creator:" + creatorId;
        String cached = redisCacheService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                log.info("Redis cache HIT for key [{}]", cacheKey);
                return objectMapper.readValue(cached, new TypeReference<List<CourseResponse>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize cached courses by creator: {}", e.getMessage());
            }
        }

        log.info("Redis cache MISS for key [{}], reading from Course DB", cacheKey);
        List<CourseResponse> courses = courseRepository.findByCreatorId(creatorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        try {
            redisCacheService.set(cacheKey, objectMapper.writeValueAsString(courses), cacheTtlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache courses by creator in Redis: {}", e.getMessage());
        }

        return courses;
    }

    @Override
    public CourseResponse updateCourse(Long courseId,
            UpdateCourseRequest request) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found"));

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Subject not found"));
            course.setSubject(subject);
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setLevel(request.getLevel());
        course.setStatus(request.getStatus());

        Course updatedCourse = courseRepository.save(course);
        CourseResponse response = mapToResponse(updatedCourse);

        evictCourseCache(courseId, course.getSubject().getSubjectId());

        return response;
    }

    @Override
    public void deleteCourse(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found"));

        Long subjectId = course.getSubject() != null ? course.getSubject().getSubjectId() : null;
        courseRepository.delete(course);

        evictCourseCache(courseId, subjectId);
    }

    private void evictCourseCache(Long courseId, Long subjectId) {
        redisCacheService.delete("course:all");
        if (courseId != null) {
            redisCacheService.delete("course:id:" + courseId);
        }
        if (subjectId != null) {
            redisCacheService.delete("course:subject:" + subjectId);
        }
        redisCacheService.deletePattern("course:creator:*");
    }

    private CourseResponse mapToResponse(Course course) {

        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .subjectId(course.getSubject() != null ? course.getSubject().getSubjectId() : null)
                .creatorId(course.getCreatorId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .level(course.getLevel())
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .build();
    }

}