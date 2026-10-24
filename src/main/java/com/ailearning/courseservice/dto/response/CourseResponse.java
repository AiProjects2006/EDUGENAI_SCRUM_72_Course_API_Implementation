package com.ailearning.courseservice.dto.response;

import com.ailearning.courseservice.enums.CourseLevel;
import com.ailearning.courseservice.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long courseId;

    private Long subjectId;

    private Long creatorId;

    private String title;

    private String description;

    private String thumbnailUrl;

    private CourseLevel level;

    private CourseStatus status;

    private LocalDateTime createdAt;
}