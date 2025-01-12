package de.hhu.exambyte.infrastructure.persistence.entity;

import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;

import de.hhu.exambyte.domain.model.Test;

import java.util.List;

@Table("tests") // Spring Data Annotation für die Zuordnung zur DB-Tabelle
public class TestEntity {

    @Id
    private final String id;
    private final String name;
    private final List<Question> questions;
    private final Test.TestStatus status;

    @PersistenceCreator
    public TestEntity(String name, String id, List<Question> questions, Test.TestStatus status) {
        this.name = name;
        this.id = id;
        this.questions = questions;
        this.status = status;
    }

    // Umwandlung von TestEntity in das Domain-Modell
    public Test toDomainModel() {
        return new Test(name, id, questions, status);
    }

    // Getter für Spring Data, falls benötigt
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Test.TestStatus getStatus() {
        return status;
    }
}
