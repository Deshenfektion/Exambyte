package de.hhu.exambyte.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import de.hhu.exambyte.persistence.model.MultipleChoiceQuestionDTO;

public interface MultipleChoiceQuestionDAO extends CrudRepository<MultipleChoiceQuestionDTO, Integer> {

}
