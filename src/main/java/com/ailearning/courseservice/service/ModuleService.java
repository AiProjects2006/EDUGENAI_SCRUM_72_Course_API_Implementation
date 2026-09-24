package com.ailearning.courseservice.service;

import com.ailearning.courseservice.dto.request.CreateModuleRequest;
import com.ailearning.courseservice.dto.request.UpdateModuleRequest;
import com.ailearning.courseservice.dto.response.ModuleResponse;

import java.util.List;

public interface ModuleService {

    ModuleResponse createModule(CreateModuleRequest request);

    List<ModuleResponse> getAllModules();

    ModuleResponse getModuleById(Long moduleId);

    ModuleResponse updateModule(Long moduleId,
                                UpdateModuleRequest request);

    void deleteModule(Long moduleId);

}