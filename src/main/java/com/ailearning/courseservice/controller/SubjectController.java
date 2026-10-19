package com.ailearning.courseservice.controller;

import com.ailearning.courseservice.dto.request.CreateSubjectRequest;
import com.ailearning.courseservice.dto.request.UpdateSubjectRequest;
import com.ailearning.courseservice.dto.response.ApiResponse;
import com.ailearning.courseservice.dto.response.SubjectResponse;
import com.ailearning.courseservice.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    /**
     * Create Subject
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {

        SubjectResponse response = subjectService.createSubject(request);

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Subject created successfully.",
                response
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    /**
     * Get All Subjects
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllSubjects() {

        List<SubjectResponse> subjects = subjectService.getAllSubjects();

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Subjects retrieved successfully.",
                subjects
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get Subject By ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getSubjectById(
            @PathVariable Long id) {

        SubjectResponse response = subjectService.getSubjectById(id);

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Subject retrieved successfully.",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update Subject
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {

        SubjectResponse response =
                subjectService.updateSubject(id, request);

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Subject updated successfully.",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete Subject
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteSubject(
            @PathVariable Long id) {

        subjectService.deleteSubject(id);

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Subject deleted successfully.",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

}