package de.hhu.exambyte.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import de.hhu.exambyte.domain.model.MultipleChoiceQuestion;
import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Question.QuestionType;
import de.hhu.exambyte.domain.model.TextbasedQuestion;

import java.util.List;

@Table("question") // Entspricht der DB-Tabelle
public class QuestionEntity {

    @Id
    private final int id;
    private final String name;
    private final QuestionType questionType;
    private final boolean correctionStatus;
    private final List<String> options; // Nur für Multiple-Choice-Fragen
    private final String description; // Nur für Textbasierte Fragen
    private int testId;

    @PersistenceCreator
    public QuestionEntity(int id, String name, QuestionType questionType, boolean correctionStatus,
            List<String> options, String description, int testId) {
        this.id = id;
        this.name = name;
        this.questionType = questionType;
        this.correctionStatus = correctionStatus;
        this.options = options;
        this.description = description;

    }

    public Question toDomainModel() {
        return switch (this.questionType) {
            case MULTIPLE_CHOICE -> new MultipleChoiceQuestion(name, options);
            case TEXT_BASED -> new TextbasedQuestion(name, description);
        };
    }

    public static QuestionEntity fromDomainModel(Question question) {
        return switch (question.getQuestionType()) {
            case MULTIPLE_CHOICE -> {
                MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
                yield new QuestionEntity(mcq.getId(), mcq.getName(), mcq.getQuestionType(),
                        mcq.getCorrectionStatus(), mcq.getOptions(), null, mcq.getTestId());
            }
            case TEXT_BASED -> {
                TextbasedQuestion tq = (TextbasedQuestion) question;
                yield new QuestionEntity(tq.getId(), tq.getName(), tq.getQuestionType(),
                        tq.getCorrectionStatus(), null, tq.getDescription(), tq.getTestId());
            }
        };
    }

    // Getter für Spring Data
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

    public int getTestId() {
        return testId;
    }
}