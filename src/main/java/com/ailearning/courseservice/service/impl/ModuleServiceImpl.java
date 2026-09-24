package com.ailearning.courseservice.service.impl;

import com.ailearning.courseservice.dto.request.CreateModuleRequest;
import com.ailearning.courseservice.dto.request.UpdateModuleRequest;
import com.ailearning.courseservice.dto.response.ModuleResponse;
import com.ailearning.courseservice.entity.Course;
import com.ailearning.courseservice.entity.Module;
import com.ailearning.courseservice.exception.ResourceNotFoundException;
import com.ailearning.courseservice.repository.CourseRepository;
import com.ailearning.courseservice.repository.ModuleRepository;
import com.ailearning.courseservice.service.ModuleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    public ModuleServiceImpl(ModuleRepository moduleRepository,
                             CourseRepository courseRepository) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public ModuleResponse createModule(CreateModuleRequest request) {

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found with id : " + request.getCourseId()));

        Module module = Module.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .course(course)
                .build();

        Module savedModule = moduleRepository.save(module);

        return mapToResponse(savedModule);
    }

    @Override
    public List<ModuleResponse> getAllModules() {

        return moduleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ModuleResponse getModuleById(Long moduleId) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Module not found with id : " + moduleId));

        return mapToResponse(module);
    }

    @Override
    public ModuleResponse updateModule(Long moduleId,
                                       UpdateModuleRequest request) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Module not found with id : " + moduleId));

        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());

        Module updatedModule = moduleRepository.save(module);

        return mapToResponse(updatedModule);
    }

    @Override
    public void deleteModule(Long moduleId) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Module not found with id : " + moduleId));

        moduleRepository.delete(module);
    }

    private ModuleResponse mapToResponse(Module module) {

        return ModuleResponse.builder()
                .moduleId(module.getModuleId())
                .title(module.getTitle())
                .description(module.getDescription())
                .courseId(module.getCourse().getCourseId())
                .build();
    }
}