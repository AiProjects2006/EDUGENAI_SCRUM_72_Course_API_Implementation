package com.ailearning.courseservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponse {

    private Long moduleId;

    private String title;

    private String description;

    private Long courseId;

}