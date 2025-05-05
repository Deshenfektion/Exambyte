package de.hhu.exambyte.dto;

/**
 * DTO zum Transportieren von Submission-Daten und zugehörigem Fragetext
 * für die Korrekturansicht.
 */
public record SubmissionForCorrectionDto(
        long submissionId,
        long questionId,
        String questionText, // Der Text der Frage
        double maxPoints, // Maximale Punkte für die Frage
        String studentGithubId,
        String submittedText // Die Antwort des Studierenden
// Optional: score und feedback, falls schon vorhanden (für Anzeige/Editieren
// durch Organizer)
// Double currentScore,
// String currentFeedback
) {
    // Einfacher Konstruktor, um aus Submission und Question zu erstellen
    // (Question müsste im Service geladen werden)
}