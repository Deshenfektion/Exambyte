package de.hhu.exambyte.dto;

/**
 * DTO zum Transportieren von Submission-Daten und zugehörigem Fragetext
 * für die Korrekturansicht.
 */
public record SubmissionForCorrectionDto(
        long submissionId,
        long questionId,
        String questionText,
        double maxPoints,
        String studentGithubId,
        String submittedText) {
}