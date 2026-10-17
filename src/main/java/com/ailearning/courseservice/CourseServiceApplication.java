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
							.description("Mathematics, arithmetic, algebra and problem solving")
							.build());
					subjectRepository.save(Subject.builder()
							.name("Science")
							.description("Natural science, biology, physics and environment")
							.build());
					subjectRepository.save(Subject.builder()
							.name("ICT")
							.description("Information and Communication Technology and digital skills")
							.build());
					subjectRepository.save(Subject.builder()
							.name("English")
							.description("English language, grammar and vocabulary")
							.build());
				}
			} catch (Exception ignored) {
			}
		};
	}
}