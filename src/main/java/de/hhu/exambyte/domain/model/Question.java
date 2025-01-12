package de.hhu.exambyte.domain.model;

public class Question {
    private final String name;
    private final String id;
    private final QuestionType type;
    private final boolean correctionStatus;

    public Question(String name, String id, QuestionType type) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.correctionStatus = false;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public QuestionType getQuestionType() {
        return this.type;
    }

    public boolean getCorrectionStatus() {
        return this.correctionStatus;
    }

    public boolean isUncorrectedTextbasedQuestion() {
        return (this.type == Question.QuestionType.TEXT_BASED && this.correctionStatus) ? true : false;
    }

    public enum QuestionType {
        MULTIPLE_CHOICE, TEXT_BASED
    }

}
