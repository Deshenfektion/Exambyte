package de.hhu.exambyte.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.hhu.exambyte.domain.model.Question;

@Repository
public interface QuestionRepository extends CrudRepository<Question, Long> {
}
