package com.ailearning.courseservice.controller;

import com.ailearning.courseservice.dto.request.CreateCourseRequest;
import com.ailearning.courseservice.dto.request.UpdateCourseRequest;
import com.ailearning.courseservice.dto.response.ApiResponse;
import com.ailearning.courseservice.dto.response.CourseResponse;
import com.ailearning.courseservice.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {

        CourseResponse response = courseService.createCourse(request);

        return new ResponseEntity<>(
                new ApiResponse(true,
                        "Course created successfully.",
                        response),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCourses() {

        List<CourseResponse> response =
                courseService.getAllCourses();

        return ResponseEntity.ok(
                new ApiResponse(true,
                        "Courses retrieved successfully.",
                        response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCourseById(
            @PathVariable Long id) {

        CourseResponse response =
                courseService.getCourseById(id);

        return ResponseEntity.ok(
                new ApiResponse(true,
                        "Course retrieved successfully.",
                        response));
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ApiResponse> getCoursesBySubject(
            @PathVariable Long subjectId) {

        List<CourseResponse> response =
                courseService.getCoursesBySubject(subjectId);

        return ResponseEntity.ok(
                new ApiResponse(true,
                        "Courses retrieved successfully.",
                        response));
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<ApiResponse> getCoursesByCreator(
            @PathVariable Long creatorId) {

        List<CourseResponse> response =
                courseService.getCoursesByCreator(creatorId);

        return ResponseEntity.ok(
                new ApiResponse(true,
                        "Courses retrieved successfully.",
                        response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {

        CourseResponse response =
                courseService.updateCourse(id, request);

        return ResponseEntity.ok(
                new ApiResponse(true,
                        "Course updated successfully.",
                        response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCourse(
            @PathVariable Long id) {

        courseService.deleteCourse(id);

        return ResponseEntity.ok(
                new ApiResponse(true,
                        "Course deleted successfully.",
                        null));
    }

}