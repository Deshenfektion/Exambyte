package de.hhu.exambyte.domain.model;

public class TextbasedQuestion implements Question {
    private final String name;
    private int id;
    private final QuestionType questionType;
    private boolean correctionStatus;
    private String description; // Speziell für TextbasedQuestions

    public TextbasedQuestion(String name, String description) {
        this.name = name;
        this.questionType = QuestionType.MULTIPLE_CHOICE;
        this.correctionStatus = false;
        this.description = description;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public QuestionType getQuestionType() {
        return this.questionType;
    }

    @Override
    public boolean getCorrectionStatus() {
        return this.correctionStatus;
    }

    @Override
    public boolean isUncorrectedTextbasedQuestion() {
        return false;
    }

    public String getDescription() {
        return this.description;
    }
}
