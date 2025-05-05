package de.hhu.exambyte.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Repräsentiert die Einreichung eines Studierenden für eine einzelne Frage
 * eines Tests.
 * Behandelt als eigener Aggregate Root.
 */
@Table("submissions") // Map record to the 'submissions' table
public record Submission(
        @Id Long id, // Primary Key

        // Referenz zum Test-Aggregat (speichert nur die Test-ID)
        AggregateReference<Test, Long> testId,

        // Referenz zum Question-Aggregat (speichert nur die Question-ID)
        AggregateReference<Question, Long> questionId,

        String studentGithubId, // Eindeutige ID des Studierenden (z.B. 'github|123456')
        String submittedText, // Die Freitext-Antwort des Studierenden (null bei MC)
        // TODO: Wie MC-Antworten speichern? Evtl. Set<AggregateReference<AnswerOption,
        // Long>> selectedOptions;

        // Bewertungsdetails (werden später von Korrektoren/System gefüllt)
        Double score, // Erreichte Punkte (null, wenn noch nicht bewertet)
        String feedback // Feedback des Korrektors (null, wenn nicht vorhanden)
) {
    // Konstruktor für neue, unbewertete Einreichungen ohne ID
    public Submission(AggregateReference<Test, Long> testId, AggregateReference<Question, Long> questionId,
            String studentGithubId, String submittedText) {
        this(null, testId, questionId, studentGithubId, submittedText, null, null);
    }

    // Methode zum Erstellen einer bewerteten Kopie (da Records immutable sind)
    public Submission withGrade(double score, String feedback) {
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, this.submittedText, score,
                feedback);
    }
}
