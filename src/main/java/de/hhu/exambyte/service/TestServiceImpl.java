package de.hhu.exambyte.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Wichtig für Operationen über mehrere Tabellen

import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.repository.SubmissionRepository;
import de.hhu.exambyte.repository.TestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TestServiceImpl implements TestService {

    private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

    private final TestRepository testRepository;
    private final SubmissionRepository submissionRepository;

    // Repositories werden per Konstruktor injiziert
    public TestServiceImpl(TestRepository testRepository, SubmissionRepository submissionRepository) {
        this.testRepository = testRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional // Stellt sicher, dass Test und alle Fragen/Optionen atomar gespeichert werden
    public Test createTest(Test test) {
        log.info("Creating new test: {}", test.title());
        // Einfache Speicherung, da das Test-Objekt das gesamte Aggregat enthält
        return testRepository.save(test);
    }

    @Override
    public Optional<Test> findTestForPreview(long testId) {
        log.debug("Finding test for preview: {}", testId);
        return testRepository.findById(testId);
    }

    @Override
    public Optional<Test> findTestForStudent(long testId, String studentGithubId) {
        log.debug("Finding test {} for student {}", testId, studentGithubId);
        Optional<Test> testOpt = testRepository.findById(testId);

        // TODO: Zeitliche Einschränkung hinzufügen
        // Aktuell wird der Test einfach zurückgegeben, wenn er existiert.
        // Später: Prüfen, ob LocalDateTime.now() >= test.startTime()
        // (oder >= test.publishTime() für Ergebnisse)

        // if (testOpt.isPresent()) {
        // Test test = testOpt.get();
        // LocalDateTime now = LocalDateTime.now();
        // if (now.isBefore(test.startTime())) {
        // log.warn("Test {} not yet started for student {}", testId, studentGithubId);
        // return Optional.empty(); // Test hat noch nicht begonnen
        // }
        // }

        return testOpt;
    }

    @Override
    public List<Test> getTestsForStudent(String studentGithubId) {
        log.debug("Getting all tests for student {}", studentGithubId);
        // TODO: Implement logic to filter tests relevant for the student
        // Simplistic approach: return all tests for now
        return (List<Test>) testRepository.findAll(); // ACHTUNG: Sehr ineffizient bei vielen Tests!
                                                      // Später spezifischere Query nötig.
    }

    @Override
    public boolean isTestActive(long testId) {
        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isPresent()) {
            Test test = testOpt.get();
            LocalDateTime now = LocalDateTime.now();
            boolean isActive = now.isAfter(test.startTime()) && now.isBefore(test.endTime());
            log.trace("Test {} active check: Start={}, End={}, Now={}, Active={}",
                    testId, test.startTime(), test.endTime(), now, isActive);
            return isActive;
        }
        log.warn("isTestActive check failed: Test {} not found", testId);
        return false; // Test nicht gefunden, also nicht aktiv
    }

    @Override
    @Transactional // Wichtig, da wir lesen (Optional<Submission>) und dann schreiben (save)
    public Submission saveOrUpdateSubmission(long testId, long questionId, String studentGithubId,
            String submittedText /* , Set<Long> selectedOptionIds */) {
        log.debug("Saving submission for test {}, question {}, student {}", testId, questionId, studentGithubId);

        // 1. Prüfen, ob der Test überhaupt aktiv ist
        if (!isTestActive(testId)) {
            log.error("Submission attempt failed: Test {} is not active.", testId);
            throw new IllegalStateException("Der Test ist derzeit nicht bearbeitbar.");
        }

        // 2. AggregateReferences erstellen (sicherer Weg, um auf die IDs zu verweisen)
        AggregateReference<Test, Long> testRef = AggregateReference.to(testId);
        AggregateReference<Question, Long> questionRef = AggregateReference.to(questionId);

        // 3. Versuchen, eine vorhandene Submission zu finden
        Optional<Submission> existingSubmissionOpt = submissionRepository.findByTestIdAndQuestionIdAndStudentGithubId(
                testRef, questionRef, studentGithubId);

        Submission submissionToSave;
        if (existingSubmissionOpt.isPresent()) {
            // 4a. Vorhandene Submission aktualisieren (neues Record-Objekt erstellen)
            Submission existing = existingSubmissionOpt.get();
            submissionToSave = new Submission(
                    existing.id(), // Wichtig: ID beibehalten!
                    existing.testId(),
                    existing.questionId(),
                    existing.studentGithubId(),
                    submittedText, // Aktualisierter Text
                    // TODO: MC-Antworten aktualisieren
                    existing.score(), // Bewertung bleibt unverändert beim Speichern durch Studenten
                    existing.feedback());
            log.trace("Updating existing submission {}", existing.id());
        } else {
            // 4b. Neue Submission erstellen
            submissionToSave = new Submission(
                    null, // ID wird von DB generiert
                    testRef,
                    questionRef,
                    studentGithubId,
                    submittedText,
                    // TODO: MC-Antworten setzen
                    null, // Keine Bewertung initial
                    null // Kein Feedback initial
            );
            log.trace("Creating new submission for test {}, question {}, student {}", testId, questionId,
                    studentGithubId);
        }

        // 5. Speichern (entweder INSERT oder UPDATE, basierend auf vorhandener ID)
        return submissionRepository.save(submissionToSave);
    }

    @Override
    public List<Submission> getSubmissionsForStudentTest(long testId, String studentGithubId) {
        log.debug("Getting submissions for student {} in test {}", studentGithubId, testId);
        AggregateReference<Test, Long> testRef = AggregateReference.to(testId);
        return submissionRepository.findByTestIdAndStudentGithubId(testRef, studentGithubId);
    }
}
