package de.hhu.exambyte.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Test {
    private final String name;
    private final String id;
    private final List<Question> questions;
    private final TestStatus status;

    public Test(String name, String id, TestStatus status) {
        this.name = name;
        this.id = id;
        this.questions = new ArrayList<>();
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public TestStatus getStatus() {
        return status;
    }

    public enum TestStatus {
        INACTIVE, ACTIVE, IN_PROGRESS, COMPLETED
    }
}
