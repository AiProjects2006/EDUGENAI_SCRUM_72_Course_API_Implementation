package com.ailearning.courseservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModuleResponse {

    private Long moduleId;

    private String title;

    private String description;

    private Long courseId;

}