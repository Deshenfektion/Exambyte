package de.hhu.exambyte.service; // Dein Paketname

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Deine Domain-Modell-Importe (passe ggf. an)
import de.hhu.exambyte.domain.model.AnswerOption; // Annahme für AnswerOption
import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.domain.model.Test;
// Deine Repository-Importe (passe ggf. an)
import de.hhu.exambyte.repository.SubmissionRepository;
import de.hhu.exambyte.repository.TestRepository;

import java.time.LocalDateTime;
import java.util.Collections; // Für leeres Set
import java.util.List;
import java.util.Optional;
import java.util.Set; // Für Set von IDs/Refs
import java.util.stream.Collectors; // Für Stream-Operationen

@Service
public class TestServiceImpl implements TestService { // Stelle sicher, dass dein Interface auch angepasst wurde

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
    @Transactional(readOnly = true) // Nur lesen
    public Optional<Test> findTestForPreview(long testId) {
        log.debug("Finding test for preview: {}", testId);
        return testRepository.findById(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Test> findTestForStudent(long testId, String studentGithubId) {
        log.debug("Finding test {} for student {}", testId, studentGithubId);
        Optional<Test> testOpt = testRepository.findById(testId);

        // TODO: Zeitliche Einschränkung hinzufügen (Prüfen ob now >= startTime)
        // Siehe vorherige Vorschläge

        return testOpt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Test> getTestsForStudent(String studentGithubId) {
        log.debug("Getting all tests for student {}", studentGithubId);
        // TODO: Implement logic to filter tests relevant for the student
        // Simplistic approach: return all tests for now
        return (List<Test>) testRepository.findAll(); // ACHTUNG: Ineffizient!
    }

    @Override
    @Transactional(readOnly = true)
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
        return false;
    }

    /**
     * Speichert oder aktualisiert die Einreichung eines Studierenden für eine
     * Frage.
     * Kann entweder Freitext ODER eine Auswahl von MC-Optionen speichern.
     *
     * @param testId            Die ID des Tests.
     * @param questionId        Die ID der Frage.
     * @param studentGithubId   Die ID des Studierenden.
     * @param submittedText     Der eingereichte Freitext (null für MC).
     * @param selectedOptionIds Ein Set der IDs der ausgewählten Antwortoptionen
     *                          (null oder leer für Freitext).
     * @return Die gespeicherte oder aktualisierte Submission.
     * @throws IllegalStateException wenn der Test nicht aktiv ist.
     */
    @Override
    @Transactional // Wichtig, da wir lesen (Optional<Submission>) und dann schreiben (save)
    public Submission saveOrUpdateSubmission(long testId, long questionId, String studentGithubId,
            String submittedText, Set<Long> selectedOptionIds) { // Signatur geändert!

        log.debug("Saving submission for test {}, question {}, student {}: Text='{}', Options={}",
                testId, questionId, studentGithubId, submittedText, selectedOptionIds);

        // 1. Prüfen, ob der Test überhaupt aktiv ist
        if (!isTestActive(testId)) {
            log.error("Submission attempt failed: Test {} is not active.", testId);
            throw new IllegalStateException("Der Test ist derzeit nicht bearbeitbar.");
        }

        // 2. AggregateReferences erstellen
        AggregateReference<Test, Long> testRef = AggregateReference.to(testId);
        AggregateReference<Question, Long> questionRef = AggregateReference.to(questionId);

        // 3. Versuchen, eine vorhandene Submission zu finden
        Optional<Submission> existingSubmissionOpt = submissionRepository.findByTestIdAndQuestionIdAndStudentGithubId(
                testRef, questionRef, studentGithubId);

        // 4. Konvertiere die übergebenen Option-IDs in AggregateReferences
        // (Nur relevant für MC-Fragen)
        final Set<AggregateReference<AnswerOption, Long>> selectedOptionRefs;
        if (selectedOptionIds != null && !selectedOptionIds.isEmpty()) {
            selectedOptionRefs = selectedOptionIds.stream()
                    .map(AggregateReference::<AnswerOption, Long>to)
                    .collect(Collectors.toSet());
        } else {
            selectedOptionRefs = Collections.emptySet(); // Leeres Set, falls null oder leer übergeben wird
        }

        Submission submissionToSave;
        if (existingSubmissionOpt.isPresent()) {
            // 4a. Vorhandene Submission aktualisieren
            Submission existing = existingSubmissionOpt.get();
            log.trace("Found existing submission with ID: {}", existing.id());

            // Entscheide, ob Freitext oder MC aktualisiert wird
            // Annahme: Entweder submittedText ODER selectedOptionIds ist relevant
            if (submittedText != null) { // Es ist ein Freitext-Update
                submissionToSave = existing.updateFreeText(submittedText);
                log.trace("Updating existing freetext submission {}", existing.id());
            } else { // Es ist ein MC-Update (auch wenn selectedOptionRefs leer ist -> Auswahl
                     // entfernt)
                submissionToSave = existing.updateSelectedOptions(selectedOptionRefs);
                log.trace("Updating existing MC submission {} with options {}", existing.id(), selectedOptionIds);
            }
        } else {
            // 4b. Neue Submission erstellen
            log.trace("No existing submission found for test {}, question {}, student {}. Creating new one.", testId,
                    questionId, studentGithubId);
            // Entscheide, ob Freitext oder MC erstellt wird
            if (submittedText != null) { // Neue Freitext-Submission
                submissionToSave = new Submission(testRef, questionRef, studentGithubId, submittedText);
                log.trace("Creating new freetext submission.");
            } else { // Neue MC-Submission
                submissionToSave = new Submission(testRef, questionRef, studentGithubId, selectedOptionRefs);
                log.trace("Creating new MC submission with options {}.", selectedOptionIds);
            }
        }

        // 5. Speichern (entweder INSERT oder UPDATE, basierend auf vorhandener ID)
        log.debug("Saving submission: {}", submissionToSave);
        Submission savedSubmission = submissionRepository.save(submissionToSave);
        log.info("Successfully saved submission with ID: {}", savedSubmission.id());
        return savedSubmission;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsForStudentTest(long testId, String studentGithubId) {
        log.debug("Getting submissions for student {} in test {}", studentGithubId, testId);
        // Rufe die Repository-Methode auf, die jetzt @Query verwendet
        // Übergib testId direkt als Long
        List<Submission> submissions = submissionRepository.findByTestIdAndStudentGithubId(testId, studentGithubId);
        log.debug("Found {} submissions for student {} in test {}", submissions.size(), studentGithubId, testId);
        // Spring Data JDBC sollte die selectedOptions für jede Submission automatisch
        // nachladen (basierend auf @MappedCollection)
        return submissions;
    }
}