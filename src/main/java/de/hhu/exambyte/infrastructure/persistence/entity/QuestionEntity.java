package de.hhu.exambyte.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import de.hhu.exambyte.domain.model.MultipleChoiceQuestion;
import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Question.QuestionType;
import de.hhu.exambyte.domain.model.TextbasedQuestion;

import java.util.List;

@Table("question") // Entspricht der DB-Tabelle
public class QuestionEntity {

    @Id
    private int id;
    private String name;
    private QuestionType questionType;
    private boolean correctionStatus;

    // Speziell für MultipleChoice
    private List<String> options;

    // Speziell für TextbasedQuestion
    private String description;

    public QuestionEntity(int id, String name, QuestionType questionType, boolean correctionStatus,
            List<String> options, String description) {
        this.id = id;
        this.name = name;
        this.questionType = questionType;
        this.correctionStatus = correctionStatus;
        this.options = options;
        this.description = description;
    }

    public Question toDomainModel() {
        if (this.questionType == QuestionType.MULTIPLE_CHOICE) {
            return new MultipleChoiceQuestion(name, options);
        } else {
            return new TextbasedQuestion(name, description);
        }
    }

    public static QuestionEntity fromDomainModel(Question question) {
        if (question instanceof MultipleChoiceQuestion mcq) {
            return new QuestionEntity(mcq.getId(), mcq.getName(), mcq.getQuestionType(),
                    mcq.getCorrectionStatus(), mcq.getOptions(), null);
        } else if (question instanceof TextbasedQuestion tq) {
            return new QuestionEntity(tq.getId(), tq.getName(), tq.getQuestionType(),
                    tq.getCorrectionStatus(), null, tq.getDescription());
        }
        throw new IllegalArgumentException("Unknown question type");
    }

    // Getter und Setter
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public boolean getCorrectionStatus() {
        return correctionStatus;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getDescription() {
        return description;
    }
}