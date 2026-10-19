package com.ailearning.courseservice.controller;

import com.ailearning.courseservice.dto.request.CreateModuleRequest;
import com.ailearning.courseservice.dto.request.UpdateModuleRequest;
import com.ailearning.courseservice.dto.response.ApiResponse;
import com.ailearning.courseservice.dto.response.ModuleResponse;
import com.ailearning.courseservice.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    /**
     * Create Module
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createModule(
            @Valid @RequestBody CreateModuleRequest request) {

        ModuleResponse response = moduleService.createModule(request);

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Module created successfully.",
                response
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    /**
     * Get All Modules
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllModules() {

        List<ModuleResponse> response = moduleService.getAllModules();

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Modules retrieved successfully.",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get Module By ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getModuleById(
            @PathVariable Long id) {

        ModuleResponse response = moduleService.getModuleById(id);

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Module retrieved successfully.",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update Module
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateModuleRequest request) {

        ModuleResponse response =
                moduleService.updateModule(id, request);

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Module updated successfully.",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete Module
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteModule(
            @PathVariable Long id) {

        moduleService.deleteModule(id);

        ApiResponse apiResponse = new ApiResponse(
                true,
                "Module deleted successfully.",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }

}