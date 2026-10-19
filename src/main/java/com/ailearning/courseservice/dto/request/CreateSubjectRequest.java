package com.ailearning.courseservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSubjectRequest {

    @NotBlank(message = "Subject name is required")
    private String name;

    private String description;
}