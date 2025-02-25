package de.hhu.exambyte.persistence.model;

import org.springframework.data.relational.core.mapping.Table;

import jakarta.persistence.Entity;

@Entity
@Table(name = "multiple_choice_question")
public class MultipleChoiceQuestionDTO extends QuestionDTO {
    private String correctAnswer;
    private String explanation;
}
