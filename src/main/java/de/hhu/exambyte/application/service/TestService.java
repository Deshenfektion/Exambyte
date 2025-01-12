package de.hhu.exambyte.application.service;

import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.infrastructure.persistence.entity.TestEntity;
import de.hhu.exambyte.infrastructure.persistence.repository.TestRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TestService {
    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    /*
     * Was gehört in den Service?
     * Orchestrierung von Domain-Objekten: Verwaltung von Tests, z. B. Erstellung
     * oder Abruf.
     * Persistenz: Speichern und Abrufen von Tests mithilfe des Repositories.
     * Externe Abhängigkeiten: Kommunikation mit externen Systemen, z. B.
     * Notification-Systeme.
     * Anwendungslogik: Umsetzung von konkreten Use Cases, die die Domänenlogik
     * verwenden.
     * Beispiele für Methoden im Service:
     * createTest(String name): Erstellt einen neuen Test und persistiert ihn.
     * getAllTests(): Gibt alle Tests aus der Datenbank zurück.
     * getTestsForStudents(): Filtert Tests für Studenten basierend auf Status.
     * deleteTest(String id): Löscht einen Test aus der Datenbank.
     */

    public Test createTest(String name, Test.TestStatus status) {
        String randomId = UUID.randomUUID().toString();

        // Überprüfen, ob die UUID bereits in der Datenbank existiert
        while (testRepository.existsById(randomId)) {
            randomId = UUID.randomUUID().toString(); // Neue UUID generieren, falls sie bereits existiert
        }

        Test newTest = new Test(name, randomId, status);
        return testRepository.save(newTest);
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