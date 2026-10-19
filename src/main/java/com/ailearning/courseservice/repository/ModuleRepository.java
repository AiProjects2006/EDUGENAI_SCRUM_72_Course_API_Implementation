package com.ailearning.courseservice.repository;

import com.ailearning.courseservice.entity.Course;
import com.ailearning.courseservice.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByCourse(Course course);

}