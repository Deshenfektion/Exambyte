package de.hhu.exambyte.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Table("questions")
public record Question(
        @Id Long id,
        String questionText,
        double maxPoints,
        QuestionType type,
        String solutionProposal,
        @MappedCollection(idColumn = "question_id", keyColumn = "id") Set<AnswerOption> options) {
    public Question(String questionText, double maxPoints, QuestionType type, String solutionProposal,
            Set<AnswerOption> options) {
        this(null, questionText, maxPoints, type, solutionProposal, options);
    }
}