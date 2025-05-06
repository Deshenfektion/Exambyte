package de.hhu.exambyte.repository;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.domain.model.Test;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JDBC Repository für das Submission-Aggregat.
 * Verwaltet die Einreichungen der Studierenden.
 */
@Repository // Markiert dies als Spring Bean und Repository
public interface SubmissionRepository extends CrudRepository<Submission, Long> {

        /**
         * Findet alle Einreichungen eines bestimmten Studierenden für einen bestimmten
         * Test.
         * BENUTZT JETZT @Query, um das Problem mit der Query-Ableitung zu umgehen.
         */
        @Query("SELECT s.id, s.test_id, s.question_id, s.student_github_id, s.submitted_text, s.score, s.feedback " +
                        "FROM submissions s " +
                        "WHERE s.test_id = :testId AND s.student_github_id = :studentGithubId")
        List<Submission> findByTestIdAndStudentGithubId(
                        @Param("testId") Long testId, // Verwende Long direkt
                        @Param("studentGithubId") String studentGithubId);

        // Standard-CRUD-Methoden sind verfügbar.

        // Benutzerdefinierte Abfragen, die wir wahrscheinlich brauchen werden:

        /**
         * Findet alle Einreichungen für einen bestimmten Test.
         */
        List<Submission> findByTestId(AggregateReference<Test, Long> testId);

        /**
         * Findet alle Einreichungen für eine bestimmte Frage über alle Tests hinweg
         * (nützlich für die Organizer-Ansicht "Alle Antworten auf Frage X anzeigen").
         */
        List<Submission> findByQuestionId(AggregateReference<Question, Long> questionId);

        /**
         * Findet alle Einreichungen eines bestimmten Studierenden für einen bestimmten
         * Test.
         */
        List<Submission> findByTestIdAndStudentGithubId(AggregateReference<Test, Long> testId, String studentGithubId);

        /**
         * Findet die spezifische Einreichung eines Studierenden für eine bestimmte
         * Frage in einem Test.
         * BENUTZT JETZT @Query.
         */
        @Query("SELECT s.id, s.test_id, s.question_id, s.student_github_id, s.submitted_text, s.score, s.feedback " +
                        "FROM submissions s " +
                        "WHERE s.test_id = :testId AND s.question_id = :questionId AND s.student_github_id = :studentGithubId")
        Optional<Submission> findByTestIdAndQuestionIdAndStudentGithubId(
                        @Param("testId") Long testId, // Verwende Long direkt
                        @Param("questionId") Long questionId, // Verwende Long direkt
                        @Param("studentGithubId") String studentGithubId);

        /**
         * Findet alle Einreichungen für eine bestimmte Frage, die noch nicht bewertet
         * wurden (score IS NULL).
         * Nützlich für Korrektoren/Organisatoren.
         * (Beachte: Könnte spezifischer sein, z.B. nur für Freitextfragen)
         */
        List<Submission> findByQuestionIdAndScoreIsNull(AggregateReference<Question, Long> questionId);

        /**
         * Findet alle Einreichungen (für Freitextfragen), die noch nicht bewertet
         * wurden.
         * Nützlich für die Korrekturübersicht.
         * Hierfür bräuchten wir einen JOIN oder eine zusätzliche Spalte in submissions,
         * um den QuestionType zu kennen,
         * oder wir filtern im Service nach dem Laden.
         * Eine alternative Abfrage könnte lauten (benötigt evtl. @Query):
         * @Query("SELECT s.* FROM submissions s JOIN questions q ON s.question_id =
         * q.id WHERE q.type = 'FREETEXT' AND s.score IS NULL")
         * List<Submission> findUnscoredFreeTextSubmissions();
         * // Oder einfacher: Finde alle ohne Score und filtere im Service
         */
        List<Submission> findByScoreIsNull();

}