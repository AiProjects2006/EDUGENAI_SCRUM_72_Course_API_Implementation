package com.ailearning.courseservice.repository;

import com.ailearning.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findBySubjectSubjectId(Long subjectId);

    List<Course> findByCreatorId(Long creatorId);

}