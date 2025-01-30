package de.hhu.exambyte.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Test {
    private String name;
    private int id;
    private List<Question> questions;
    private TestStatus status;

    // Standardkonstruktor für Spring
    public Test() {
        this.questions = new ArrayList<>();
    }

    public Test(String name, TestStatus status) {
        this.name = name;
        this.questions = new ArrayList<>();
        this.status = status;
    }

    public Test(String name, int id, List<Question> questions, TestStatus status) {
        this.name = name;
        this.id = id;
        this.questions = questions;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public enum TestStatus {
        INACTIVE, ACTIVE, IN_PROGRESS, COMPLETED
    }

    public boolean isCompleted() {
        return (this.status == TestStatus.COMPLETED);
    }

    public boolean hasUncorrectedTextbasedQuestions() {
        for (Question question : questions) {
            if (question.isUncorrectedTextbasedQuestion()) {
                return true;
            }
        }
        return false;
    }
}