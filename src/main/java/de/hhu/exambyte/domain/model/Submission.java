package de.hhu.exambyte.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Collections; // Import für leeres Set
import java.util.Set; // Import für Set

/**
 * Repräsentiert die Einreichung eines Studierenden für eine einzelne Frage
 * eines Tests.
 * Behandelt als eigener Aggregate Root.
 */
@Table("submissions")
public record Submission(
        @Id Long id,
        AggregateReference<Test, Long> testId,
        AggregateReference<Question, Long> questionId,
        String studentGithubId,
        String submittedText, // Für Freitext

        // NEU: Set von Referenzen auf ausgewählte Antwortoptionen (für MC)
        // Bezieht sich auf die Zwischentabelle 'submission_selected_options'.
        // 'submission_id' ist die Spalte in der Zwischentabelle, die auf diese
        // Submission verweist.
        // 'answer_option_id' ist die Spalte in der Zwischentabelle, die auf die
        // AnswerOption verweist.
        @MappedCollection(idColumn = "submission_id", keyColumn = "answer_option_id") Set<AggregateReference<AnswerOption, Long>> selectedOptions,

        Double score,
        String feedback) {
    // Konstruktor für neue, unbewertete Freitext-Einreichungen
    public Submission(AggregateReference<Test, Long> testId, AggregateReference<Question, Long> questionId,
            String studentGithubId, String submittedText) {
        this(null, testId, questionId, studentGithubId, submittedText, Collections.emptySet(), null, null);
    }

    // Konstruktor für neue, unbewertete MC-Einreichungen
    public Submission(AggregateReference<Test, Long> testId, AggregateReference<Question, Long> questionId,
            String studentGithubId, Set<AggregateReference<AnswerOption, Long>> selectedOptions) {
        this(null, testId, questionId, studentGithubId, null, selectedOptions, null, null);
    }

    // Methode zum Erstellen einer bewerteten Kopie
    public Submission withGrade(double score, String feedback) {
        // Behält die ursprünglichen Antworten (Text ODER Optionen) bei
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, this.submittedText,
                this.selectedOptions, score, feedback);
    }

    // Methode zum Aktualisieren der Antworten (z.B. wenn Student erneut speichert)
    // Für Freitext:
    public Submission updateFreeText(String newText) {
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, newText,
                this.selectedOptions, this.score, this.feedback);
    }

    // Für MC:
    public Submission updateSelectedOptions(Set<AggregateReference<AnswerOption, Long>> newOptions) {
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, this.submittedText,
                newOptions, this.score, this.feedback);
    }
}