package com.ailearning.courseservice;

import com.ailearning.courseservice.entity.Subject;
import com.ailearning.courseservice.repository.SubjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CourseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedSubjects(SubjectRepository subjectRepository) {
		return args -> {
			try {
				if (subjectRepository.count() == 0) {
					subjectRepository.save(Subject.builder()
							.name("Mathematics")
							.description("Mathematics, arithmetic, and problem solving for Grades 3-5")
							.build());
					subjectRepository.save(Subject.builder()
							.name("Environmental Studies")
							.description("Environmental studies, nature, surroundings and community")
							.build());
					subjectRepository.save(Subject.builder()
							.name("ICT")
							.description("Information and Communication Technology, computer basics and digital literacy")
							.build());
				} else {
					if (subjectRepository.findByName("Environmental Studies").isEmpty()) {
						subjectRepository.save(Subject.builder()
								.name("Environmental Studies")
								.description("Environmental studies, nature, surroundings and community")
								.build());
					}
				}
			} catch (Exception ignored) {
			}
		};
	}
}