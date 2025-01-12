package de.hhu.exambyte.application.service;

import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.infrastructure.persistence.repository.TestRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestService {
    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public Test createTest(Test test) {
        return testRepository.save(test);
    }

    public List<Test> getAllTests() {
        return (List<Test>) testRepository.findAll();
    }

    public List<Test> getTestsForStudents() {
        // Tests für Studenten: veröffentlicht + vor Endzeitpunkt
        return getAllTests().stream()
                .filter(test -> test.getStatus() == Test.TestStatus.ACTIVE
                        || test.getStatus() == Test.TestStatus.IN_PROGRESS)
                .collect(Collectors.toList());
    }

    public List<Test> getTestsForOrganizers() {
        // Tests für Organisatoren: Alle Tests
        return getAllTests();
    }

    // public List<Test> getTestsForCorrectors() {
    // // Tests für Korrektoren: Unkorrigierte Freitextaufgaben
    // return getAllTests().stream()
    // .filter(test -> test.getStatus() == Test.TestStatus.COMPLETED)
    // .filter(test -> test.getQuestions().stream()
    // .anyMatch(question -> question.isUncorrectedTextbasedQuestion()))
    // .collect(Collectors.toList());
    // }

    public Test getTestById(String id) {
        return testRepository.findById(id).orElseThrow(() -> new RuntimeException("Test not found with id: " + id));
    }

}