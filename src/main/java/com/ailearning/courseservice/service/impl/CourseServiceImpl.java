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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             SubjectRepository subjectRepository) {

        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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

        return mapToResponse(savedCourse);
    }

    @Override
    public List<CourseResponse> getAllCourses() {

        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CourseResponse getCourseById(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found"));

        return mapToResponse(course);
    }

    @Override
    public List<CourseResponse> getCoursesBySubject(Long subjectId) {

        return courseRepository.findBySubjectSubjectId(subjectId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponse> getCoursesByCreator(Long creatorId) {

        return courseRepository.findByCreatorId(creatorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CourseResponse updateCourse(Long courseId,
                                       UpdateCourseRequest request) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found"));

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Subject not found"));
            course.setSubject(subject);
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setLevel(request.getLevel());
        course.setStatus(request.getStatus());

        Course updatedCourse = courseRepository.save(course);

        return mapToResponse(updatedCourse);
    }

    @Override
    public void deleteCourse(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found"));

        courseRepository.delete(course);
    }

    private CourseResponse mapToResponse(Course course) {

        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .subjectId(course.getSubject().getSubjectId())
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