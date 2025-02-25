package de.hhu.exambyte.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import de.hhu.exambyte.persistence.model.TextbasedQuestionDTO;

public interface TextbasedQuestionQuestionDAO extends CrudRepository<TextbasedQuestionDTO, Integer> {

}
