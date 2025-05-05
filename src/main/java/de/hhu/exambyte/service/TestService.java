package de.hhu.exambyte.service;

import java.util.List;
import java.util.Optional;

import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.domain.model.Test;

/**
 * Service-Interface für die Verwaltung und Durchführung von Tests.
 */
public interface TestService {

    /**
     * Erstellt einen neuen Test inklusive seiner Fragen und Antwortmöglichkeiten.
     *
     * @param test Das zu speichernde Test-Objekt (enthält Fragen etc.).
     * @return Der gespeicherte Test mit generierter ID.
     */
    Test createTest(Test test);

    /**
     * Findet einen Test anhand seiner ID für die Vorschau durch einen Organisator.
     * Gibt alle Details zurück, unabhängig von Zeitbeschränkungen.
     *
     * @param testId Die ID des Tests.
     * @return Ein Optional, das den Test enthält, wenn gefunden.
     */
    Optional<Test> findTestForPreview(long testId);

    /**
     * Findet einen Test anhand seiner ID für einen Studierenden.
     * Berücksichtigt Sichtbarkeitsregeln (z.B. erst nach Startzeit sichtbar).
     * // TODO: Zeitliche Einschränkung implementieren
     *
     * @param testId          Die ID des Tests.
     * @param studentGithubId Die ID des anfragenden Studierenden.
     * @return Ein Optional, das den Test enthält, wenn er für den Studierenden
     *         sichtbar ist.
     */
    Optional<Test> findTestForStudent(long testId, String studentGithubId);

    /**
     * Findet alle Tests, die für einen Studierenden relevant sind (z.B. für das
     * Dashboard).
     * // TODO: Implementieren (evtl. nur Tests zurückgeben, die gestartet sind oder
     * deren Ergebnis veröffentlicht wurde)
     *
     * @param studentGithubId Die ID des Studierenden.
     * @return Eine Liste von Tests.
     */
    List<Test> getTestsForStudent(String studentGithubId);

    /**
     * Prüft, ob ein Test aktuell für einen Studierenden bearbeitbar ist (zwischen
     * Start- und Endzeit).
     *
     * @param testId Die ID des Tests.
     * @return true, wenn der Test gerade aktiv ist, sonst false.
     */
    boolean isTestActive(long testId);

    /**
     * Speichert oder aktualisiert die Einreichung eines Studierenden für eine
     * bestimmte Frage.
     * Prüft, ob der Test gerade aktiv ist.
     *
     * @param testId          Die ID des Tests.
     * @param questionId      Die ID der Frage.
     * @param studentGithubId Die ID des Studierenden.
     * @param submittedText   Der eingereichte Freitext (oder null).
     *                        // TODO: MC-Antworten übergeben
     * @return Die gespeicherte oder aktualisierte Submission.
     * @throws IllegalStateException wenn der Test nicht aktiv ist.
     */
    Submission saveOrUpdateSubmission(long testId, long questionId, String studentGithubId, String submittedText /*
                                                                                                                  * ,
                                                                                                                  * Set<
                                                                                                                  * Long>
                                                                                                                  * selectedOptionIds
                                                                                                                  */);

    /**
     * Lädt alle Einreichungen eines Studierenden für einen bestimmten Test.
     * Nützlich, um den Test-View mit bereits gegebenen Antworten zu befüllen.
     *
     * @param testId          Die ID des Tests.
     * @param studentGithubId Die ID des Studierenden.
     * @return Eine Liste der Einreichungen.
     */
    List<Submission> getSubmissionsForStudentTest(long testId, String studentGithubId);

}
