package de.hhu.exambyte.domain.model;

public interface Question {
    String getName();

    int getId();

    int getTestId();

    QuestionType getQuestionType();

    boolean getCorrectionStatus();

    // Wichtig für Korrektoren
    boolean isUncorrectedTextbasedQuestion();

    enum QuestionType {
        MULTIPLE_CHOICE, TEXT_BASED
    }
}
