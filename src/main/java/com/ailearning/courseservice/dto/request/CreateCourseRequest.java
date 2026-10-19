package com.ailearning.courseservice.dto.request;

import com.ailearning.courseservice.enums.CourseLevel;
import com.ailearning.courseservice.enums.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCourseRequest {

    @NotNull
    private Long subjectId;

    @NotNull
    private Long creatorId;

    @NotBlank
    private String title;

    private String description;

    private String thumbnailUrl;

    @NotNull
    private CourseLevel level;

    @NotNull
    private CourseStatus status;
}