package de.hhu.exambyte.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.hhu.exambyte.domain.model.Question;

@Repository
public interface QuestionRepository extends CrudRepository<Question, Integer> {
    Optional<Question> findFirstByTestIdOrderByIdAsc(int testId);

    Optional<Question> findFirstByTestIdAndIdGreaterThanOrderByIdAsc(int testId, int currentQuestionId);

    List<Question> findByTestIdOrderByIdAsc(int testId);
}
