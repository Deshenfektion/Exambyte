package de.hhu.exambyte.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import de.hhu.exambyte.dto.SubmissionForCorrectionDto;

/**
 * Service-Interface für die manuelle Korrektur von Einreichungen.
 */
public interface CorrectionService {

    /**
     * Findet alle Einreichungen für Freitextfragen, die noch nicht bewertet wurden.
     * Gibt DTOs zurück, die auch den Fragetext enthalten.
     *
     * @return Eine Liste von DTOs zur Korrektur.
     */
    List<SubmissionForCorrectionDto> findUnscoredFreitextSubmissions();

    /**
     * Holt die Details einer spezifischen Einreichung zur Korrektur.
     *
     * @param submissionId Die ID der Einreichung.
     * @return Ein Optional, das ein DTO mit Submission- und Fragendetails enthält.
     */
    Optional<SubmissionForCorrectionDto> getSubmissionForCorrection(long submissionId);

    /**
     * Speichert die Bewertung (Punkte und Feedback) für eine Einreichung.
     * Validiert, dass Feedback vorhanden ist, wenn nicht die Maximalpunktzahl
     * vergeben wird.
     *
     * @param submissionId Die ID der zu bewertenden Einreichung.
     * @param score        Die vergebenen Punkte.
     * @param feedback     Das Feedback des Korrektors.
     * @param correctorId  Die ID des Korrektors (optional, für
     *                     Logging/Nachvollziehbarkeit).
     * @throws IllegalArgumentException wenn die Validierung fehlschlägt (z.B.
     *                                  Feedback fehlt).
     * @throws NoSuchElementException   wenn die Submission oder die zugehörige
     *                                  Frage nicht gefunden wird.
     */
    void saveGrade(long submissionId, double score, String feedback, String correctorId)
            throws IllegalArgumentException;

}