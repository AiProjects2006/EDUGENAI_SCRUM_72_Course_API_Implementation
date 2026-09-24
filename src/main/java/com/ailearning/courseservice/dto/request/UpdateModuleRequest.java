package com.ailearning.courseservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateModuleRequest {

    @NotBlank(message = "Module title is required")
    private String title;

    private String description;

}