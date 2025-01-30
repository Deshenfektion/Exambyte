package de.hhu.exambyte;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import de.hhu.exambyte.application.service.TestService;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.domain.model.Test.TestStatus;
import de.hhu.exambyte.infrastructure.persistence.entity.TestEntity;
import de.hhu.exambyte.infrastructure.persistence.repository.TestRepository;

@EnableJdbcRepositories
@SpringBootApplication
public class ExambyteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExambyteApplication.class, args);
	}

	@Bean
	CommandLineRunner main(TestRepository testRepository, TestService testService) {
		return args -> {
			Test test1 = testService.createTest("Test_1", TestStatus.INACTIVE);
			System.out.println("Mein erster Test: " + test1);
		};
	}
}
