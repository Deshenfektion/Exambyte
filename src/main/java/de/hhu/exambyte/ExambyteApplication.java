package de.hhu.exambyte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@EnableJdbcRepositories
@SpringBootApplication
public class ExambyteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExambyteApplication.class, args);
	}

}
