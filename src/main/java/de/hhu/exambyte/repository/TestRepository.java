package de.hhu.exambyte.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.hhu.exambyte.domain.model.Test;

@Repository
public interface TestRepository extends CrudRepository<Test, Long> {

}