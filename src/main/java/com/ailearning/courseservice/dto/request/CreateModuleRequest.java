package com.ailearning.courseservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateModuleRequest {

    @NotBlank(message = "Module title is required")
    private String title;

    private String description;

    @NotNull(message = "Course ID is required")
    private Long courseId;

}