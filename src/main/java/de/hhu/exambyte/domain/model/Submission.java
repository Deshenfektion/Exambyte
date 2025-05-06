package de.hhu.exambyte.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors; // Importieren

@Table("submissions")
public record Submission(
        @Id Long id,
        AggregateReference<Test, Long> testId,
        AggregateReference<Question, Long> questionId,
        String studentGithubId,
        String submittedText, // Für Freitext

        // *** GEÄNDERT: Typ des Sets und keyColumn ***
        // idColumn verweist auf die Spalte in der Zwischentabelle, die UNSERE ID
        // enthält (submission_id).
        // keyColumn verweist auf die Spalte in der Zwischentabelle, die die ID der
        // ANDEREN Seite enthält (answer_option_id).
        @MappedCollection(idColumn = "submission_id", keyColumn = "answer_option_id") Set<SubmissionSelectedOptionRef> selectedOptions, // <--
                                                                                                                                        // Typ
                                                                                                                                        // geändert!

        Double score,
        String feedback) {

    // Konstruktor für Freitext bleibt (selectedOptions wird leer sein)
    public Submission(AggregateReference<Test, Long> testId, AggregateReference<Question, Long> questionId,
            String studentGithubId, String submittedText) {
        this(null, testId, questionId, studentGithubId, submittedText, Collections.emptySet(), null, null);
    }

    // Konstruktor für MC anpassen
    public Submission(AggregateReference<Test, Long> testId, AggregateReference<Question, Long> questionId,
            String studentGithubId, Set<SubmissionSelectedOptionRef> selectedOptionsRefs) {
        this(null, testId, questionId, studentGithubId, null,
                selectedOptionsRefs != null ? selectedOptionsRefs : Collections.emptySet(), null, null);
    }

    // Methode zum Erstellen einer bewerteten Kopie
    public Submission withGrade(double score, String feedback) {
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, this.submittedText,
                this.selectedOptions, score, feedback);
    }

    // Update-Methoden anpassen
    public Submission updateFreeText(String newText) {
        // Wenn Freitext aktualisiert wird, sollten MC-Optionen gelöscht werden?
        // Hier wird angenommen, dass sie beibehalten werden (falls gemischt möglich
        // wäre).
        // Sicherer wäre es evtl., hier Collections.emptySet() zu übergeben.
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, newText,
                Collections.emptySet(), this.score, this.feedback); // Setze MC-Optionen zurück bei Freitext-Update
    }

    public Submission updateSelectedOptions(Set<SubmissionSelectedOptionRef> newOptions) {
        // Wenn MC aktualisiert wird, sollte Freitext gelöscht werden?
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, null, // Setze Text auf null
                newOptions != null ? newOptions : Collections.emptySet(), this.score, this.feedback);
    }

    // Statische Hilfsmethode zur Konvertierung (kann auch im Service stehen)
    public static Set<SubmissionSelectedOptionRef> longsToRefs(Set<Long> optionIds) {
        if (optionIds == null)
            return Collections.emptySet();
        return optionIds.stream().map(SubmissionSelectedOptionRef::new).collect(Collectors.toSet());
    }
}