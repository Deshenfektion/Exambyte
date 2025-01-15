package de.hhu.exambyte.domain.model;

import java.util.List;

public class MultipleChoiceQuestion implements Question {
    private final String name;
    private String id;
    private final QuestionType questionType;
    private boolean correctionStatus;
    private List<String> options; // Speziell für MultipleChoiceQuestion

    public MultipleChoiceQuestion(String name, List<String> options) {
        this.name = name;
        this.questionType = QuestionType.MULTIPLE_CHOICE;
        this.correctionStatus = false;
        this.options = options;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getId() {
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

    public List<String> getOptions() {
        return this.options;
    }
}
