package de.hhu.exambyte.infrastructure.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import de.hhu.exambyte.domain.model.Test;

public interface TestRepository extends CrudRepository<Test, String> {

}
