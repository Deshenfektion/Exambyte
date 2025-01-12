package de.hhu.exambyte.infrastructure.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import de.hhu.exambyte.domain.model.Question;

public interface QuestionRepository extends CrudRepository<Question, String> {

}
