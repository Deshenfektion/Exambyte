package de.hhu.exambyte.domain.model;

import org.springframework.data.relational.core.mapping.Table;

/**
 * Repräsentiert einen Eintrag in der Zwischentabelle
 * 'submission_selected_options',
 * der eine ausgewählte Antwortoption für eine Submission darstellt.
 * Dies ist ein Wertobjekt innerhalb des Submission-Aggregats.
 */
@Table("submission_selected_options") // Wichtig: Der Tabellenname für die Relation
public record SubmissionSelectedOptionRef(
        // Keine @Id hier!
        // Enthält die ID der referenzierten AnswerOption.
        // Der Spaltenname 'answer_option_id' in der DB wird durch
        // @MappedCollection.keyColumn definiert.
        Long answerOptionId) {
}