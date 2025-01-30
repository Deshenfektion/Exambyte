package de.hhu.exambyte.application.service;

import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.infrastructure.persistence.entity.TestEntity;
import de.hhu.exambyte.infrastructure.persistence.repository.TestRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestService {
    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public Test createTest(String name, Test.TestStatus status) {
        // Erstelle ein TestEntity
        TestEntity newTestEntity = new TestEntity(name, 0, null, status);
        // Speichere das Entity und konvertiere es zurück ins Domain-Modell
        TestEntity savedTestEntity = testRepository.save(newTestEntity);
        return savedTestEntity.toDomainModel();
    }

    public List<Test> getAllTests() {
        // Finde alle Tests und konvertiere sie in das Domain-Modell
        return (List<Test>) testRepository.findAll().stream()
                .map(TestEntity::toDomainModel)
                .collect(Collectors.toList());
    }

    public List<Test> getTestsForStudents() {
        return getAllTests().stream()
                .filter(test -> test.getStatus() == Test.TestStatus.ACTIVE
                        || test.getStatus() == Test.TestStatus.IN_PROGRESS)
                .collect(Collectors.toList());
    }

    public List<Test> getTestsForOrganizers() {
        return getAllTests();
    }

    public List<Test> getTestsForCorrectors() {
        return getAllTests().stream()
                .filter(test -> test.isCompleted())
                .filter(test -> test.hasUncorrectedTextbasedQuestions())
                .collect(Collectors.toList());
    }

    public Test getTestById(int id) {
        TestEntity testEntity = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found with id: " + id));
        return testEntity.toDomainModel();
    }
}