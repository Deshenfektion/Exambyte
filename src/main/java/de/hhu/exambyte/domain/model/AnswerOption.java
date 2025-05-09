package de.hhu.exambyte.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Repräsentiert eine Antwortmöglichkeit für eine Multiple-Choice-Frage.
 * Gehört zur Question und damit zum Test-Aggregat.
 */
@Table("answer_options")
public record AnswerOption(
        @Id Long id,
        String optionText, // Text der Antwortmöglichkeit (HTML erlaubt)
        boolean correct) {
    public AnswerOption(String optionText, boolean correct) {
        this(null, optionText, correct);
    }
}
