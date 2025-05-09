package de.hhu.exambyte.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.QuestionType;
import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.dto.SubmissionForCorrectionDto;
import de.hhu.exambyte.repository.QuestionRepository;
import de.hhu.exambyte.repository.SubmissionRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CorrectionServiceImpl implements CorrectionService {

    private static final Logger log = LoggerFactory.getLogger(CorrectionServiceImpl.class);

    private final SubmissionRepository submissionRepository;
    private final QuestionRepository questionRepository;

    public CorrectionServiceImpl(SubmissionRepository submissionRepository, QuestionRepository questionRepository) {
        this.submissionRepository = submissionRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional(readOnly = true) // Nur Lesezugriff
    public List<SubmissionForCorrectionDto> findUnscoredFreitextSubmissions() {
        log.debug("Finding unscored freetext submissions...");
        List<Submission> unscoredSubmissions = submissionRepository.findByScoreIsNull();
        log.debug("Found {} submissions with score=NULL", unscoredSubmissions.size());

        // Filtere nach Freitext und lade Fragendetails, um DTOs zu bauen
        return unscoredSubmissions.stream()
                .map(submission -> {
                    // Lade die zugehörige Frage
                    Optional<Question> questionOpt = questionRepository.findById(submission.questionId().getId());
                    return questionOpt.map(question -> {
                        // Nur Freitextfragen berücksichtigen
                        if (question.type() == QuestionType.FREETEXT) {
                            return new SubmissionForCorrectionDto(
                                    submission.id(),
                                    question.id(),
                                    question.questionText(),
                                    question.maxPoints(),
                                    submission.studentGithubId(),
                                    submission.submittedText());
                        } else {
                            return null; // Nicht-Freitext ignorieren
                        }
                    }).orElse(null); // Frage nicht gefunden -> ignorieren
                })
                .filter(dto -> dto != null) // Entferne null-Einträge (andere Typen, fehlende Fragen)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubmissionForCorrectionDto> getSubmissionForCorrection(long submissionId) {
        log.debug("Getting submission {} for correction", submissionId);
        return submissionRepository.findById(submissionId)
                .flatMap(submission -> // flatMap, weil findById wieder Optional zurückgibt
                questionRepository.findById(submission.questionId().getId())
                        .map(question -> new SubmissionForCorrectionDto(
                                submission.id(),
                                question.id(),
                                question.questionText(),
                                question.maxPoints(),
                                submission.studentGithubId(),
                                submission.submittedText()
                        // Optional: Hier könnte man auch submission.score() und submission.feedback()
                        // mitgeben
                        )));
    }

    @Override
    @Transactional // Schreibender Zugriff
    public void saveGrade(long submissionId, double score, String feedback, String correctorId)
            throws IllegalArgumentException {
        log.info("Corrector {} grading submission {}: Score={}, Feedback='{}'", correctorId, submissionId, score,
                feedback);

        // 1. Submission laden
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> {
                    log.error("Grading failed: Submission {} not found.", submissionId);
                    return new NoSuchElementException("Einreichung mit ID " + submissionId + " nicht gefunden.");
                });

        // 2. Zugehörige Frage laden (für maxPoints und Validierung)
        Question question = questionRepository.findById(submission.questionId().getId())
                .orElseThrow(() -> {
                    log.error("Grading failed: Question {} for submission {} not found.",
                            submission.questionId().getId(), submissionId);
                    return new NoSuchElementException(
                            "Zugehörige Frage für Einreichung " + submissionId + " nicht gefunden.");
                });

        // 3. Validieren
        if (score < 0 || score > question.maxPoints()) {
            log.warn("Grading validation failed for submission {}: Score {} out of range (0-{}).", submissionId, score,
                    question.maxPoints());
            throw new IllegalArgumentException("Punktzahl muss zwischen 0 und " + question.maxPoints() + " liegen.");
        }

        // Die Anforderung "Alle nicht-leeren Einreichungen, die nicht mit maximaler
        // Punktzahl bewertet werden, müssen immer einen Feedbacktext enthalten."
        boolean isEmptySubmission = !StringUtils.hasText(submission.submittedText());
        boolean isMaxScore = score == question.maxPoints();

        if (!isEmptySubmission && !isMaxScore && !StringUtils.hasText(feedback)) {
            log.warn(
                    "Grading validation failed for submission {}: Feedback is required for non-empty submission with non-max score ({} < {}).",
                    submissionId, score, question.maxPoints());
            throw new IllegalArgumentException(
                    "Feedback ist erforderlich, wenn nicht die maximale Punktzahl vergeben wird und die Einreichung nicht leer ist.");
        }

        // 4. Bewertung speichern (neues Submission-Record erstellen)
        Submission gradedSubmission = submission.withGrade(score, feedback);
        submissionRepository.save(gradedSubmission);
        log.info("Successfully graded submission {}", submissionId);
    }
}
