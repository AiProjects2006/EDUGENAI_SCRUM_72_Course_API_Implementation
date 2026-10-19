package com.ailearning.courseservice.service;

import com.ailearning.courseservice.dto.request.CreateCourseRequest;
import com.ailearning.courseservice.dto.request.UpdateCourseRequest;
import com.ailearning.courseservice.dto.response.CourseResponse;

import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CreateCourseRequest request);

    List<CourseResponse> getAllCourses();

    CourseResponse getCourseById(Long courseId);

    List<CourseResponse> getCoursesBySubject(Long subjectId);

    List<CourseResponse> getCoursesByCreator(Long creatorId);

    CourseResponse updateCourse(Long courseId,
                                UpdateCourseRequest request);

    void deleteCourse(Long courseId);

}