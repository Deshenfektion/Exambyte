package de.hhu.exambyte.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

/**
 * Repräsentiert eine Frage innerhalb eines Tests.
 * Gehört zum Test-Aggregat.
 */
@Table("questions") // Map record to the 'questions' table
public record Question(
        @Id Long id, // Primary Key
        String questionText, // Der Text der Frage (HTML erlaubt)
        double maxPoints,
        QuestionType type,
        String solutionProposal, // Lösungsvorschlag (für Freitext, angezeigt nach Testende)

        // One-to-Many-Beziehung für MC-Fragen: Eine Frage hat mehrere Antwortoptionen.
        // Wird nur für Fragen vom Typ MC relevant sein.
        // 'question_id' ist die Spalte in der 'answer_options' Tabelle.
        @MappedCollection(idColumn = "question_id", keyColumn = "id") Set<AnswerOption> options // Dieses Set ist leer
                                                                                                // für FREETEXT-Fragen
) {
    // Konstruktor für neue Fragen ohne ID
    public Question(String questionText, double maxPoints, QuestionType type, String solutionProposal,
            Set<AnswerOption> options) {
        this(null, questionText, maxPoints, type, solutionProposal, options);
    }
}