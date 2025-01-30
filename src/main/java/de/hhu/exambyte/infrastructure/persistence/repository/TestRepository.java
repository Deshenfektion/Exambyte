package de.hhu.exambyte.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.hhu.exambyte.infrastructure.persistence.entity.TestEntity;

@Repository
public interface TestRepository extends CrudRepository<TestEntity, Integer> {

    List<TestEntity> findAll();
}
