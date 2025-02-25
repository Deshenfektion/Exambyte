package de.hhu.exambyte.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import de.hhu.exambyte.persistence.model.TestDTO;

public interface TestDAO extends CrudRepository<TestDTO, Integer> {

}
