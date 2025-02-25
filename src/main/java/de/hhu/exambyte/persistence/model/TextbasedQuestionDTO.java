package de.hhu.exambyte.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "textbased_question")
public class TextbasedQuestionDTO extends QuestionDTO {
    private String optionalFeedback;
}
