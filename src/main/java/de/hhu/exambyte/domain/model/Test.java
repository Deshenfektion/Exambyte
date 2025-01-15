package de.hhu.exambyte.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Test {
    private final String name;
    // Wird von der Datenbank gesetzt via SERIAL PRIMARY KEY
    private String id;
    private final List<Question> questions;
    private final TestStatus status;

    public Test(String name, TestStatus status) {
        this.name = name;
        this.questions = new ArrayList<>();
        this.status = status;
    }

    public Test(String name, String id, List<Question> questions, TestStatus status) {
        this.name = name;
        this.id = id;
        this.questions = questions;
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

    /*
     * Was gehört ins Domain Model?
     * Eigenschaften und Status: Attribute wie name, id, status.
     * Invariants: Regeln, die immer erfüllt sein müssen, z. B.
     * "Ein Test muss mindestens eine Frage haben".
     * Verhaltenslogik: Methoden, die Logik bezogen auf den Zustand des Tests
     * implementieren.
     * Zustandsänderungen: Änderungen am Status oder Zustand der Domäne (z. B. Test
     * starten oder abschließen).
     */

    /*
     * Beispiele für Methoden im Domain Model:
     * activate(): Aktiviert einen Test, wenn er noch inaktiv ist.
     * complete(): Markiert einen Test als abgeschlossen.
     * addQuestion(Question question): Fügt eine neue Frage hinzu.
     * isValid(): Überprüft, ob der Test den Validierungsregeln entspricht.
     */
}
