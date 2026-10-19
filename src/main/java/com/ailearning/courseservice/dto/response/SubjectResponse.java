package com.ailearning.courseservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectResponse {

    private Long subjectId;

    private String name;

    private String description;
}