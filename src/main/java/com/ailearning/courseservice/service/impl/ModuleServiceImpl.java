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
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.course.ttl-seconds:86400}")
    private long cacheTtlSeconds;

    public ModuleServiceImpl(ModuleRepository moduleRepository,
                             CourseRepository courseRepository,
                             RedisCacheService redisCacheService,
                             ObjectMapper objectMapper) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.redisCacheService = redisCacheService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ModuleResponse createModule(CreateModuleRequest request) {

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with id : " + request.getCourseId()));

        Module module = Module.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .course(course)
                .build();

        Module savedModule = moduleRepository.save(module);
        ModuleResponse response = mapToResponse(savedModule);

        evictModuleCache(savedModule.getModuleId());

        return response;
    }

    @Override
    public List<ModuleResponse> getAllModules() {
        String cacheKey = "module:all";
        String cached = redisCacheService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                log.info("Redis cache HIT for key [{}]", cacheKey);
                return objectMapper.readValue(cached, new TypeReference<List<ModuleResponse>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize cached modules: {}", e.getMessage());
            }
        }

        log.info("Redis cache MISS for key [{}], reading from Course DB", cacheKey);
        List<ModuleResponse> modules = moduleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        try {
            redisCacheService.set(cacheKey, objectMapper.writeValueAsString(modules), cacheTtlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache modules in Redis: {}", e.getMessage());
        }

        return modules;
    }

    @Override
    public ModuleResponse getModuleById(Long moduleId) {
        String cacheKey = "module:id:" + moduleId;
        String cached = redisCacheService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                log.info("Redis cache HIT for key [{}]", cacheKey);
                return objectMapper.readValue(cached, ModuleResponse.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize cached module: {}", e.getMessage());
            }
        }

        log.info("Redis cache MISS for key [{}], reading from Course DB", cacheKey);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id : " + moduleId));

        ModuleResponse response = mapToResponse(module);

        try {
            redisCacheService.set(cacheKey, objectMapper.writeValueAsString(response), cacheTtlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache module in Redis: {}", e.getMessage());
        }

        return response;
    }

    @Override
    public ModuleResponse updateModule(Long moduleId,
            UpdateModuleRequest request) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id : " + moduleId));

        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());

        Module updatedModule = moduleRepository.save(module);
        ModuleResponse response = mapToResponse(updatedModule);

        evictModuleCache(moduleId);

        return response;
    }

    @Override
    public void deleteModule(Long moduleId) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Module not found with id : " + moduleId));

        moduleRepository.delete(module);

        evictModuleCache(moduleId);
    }

    private void evictModuleCache(Long moduleId) {
        redisCacheService.delete("module:all");
        if (moduleId != null) {
            redisCacheService.delete("module:id:" + moduleId);
        }
    }

    private ModuleResponse mapToResponse(Module module) {

        return ModuleResponse.builder()
                .moduleId(module.getModuleId())
                .title(module.getTitle())
                .description(module.getDescription())
                .courseId(module.getCourse() != null ? module.getCourse().getCourseId() : null)
                .build();
    }
}