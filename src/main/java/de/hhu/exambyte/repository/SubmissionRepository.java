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

@Repository
public interface SubmissionRepository extends CrudRepository<Submission, Long> {

        @Query("SELECT s.id, s.test_id, s.question_id, s.student_github_id, s.submitted_text, s.score, s.feedback " +
                        "FROM submissions s " +
                        "WHERE s.test_id = :testId AND s.student_github_id = :studentGithubId")
        List<Submission> findByTestIdAndStudentGithubId(
                        @Param("testId") Long testId,
                        @Param("studentGithubId") String studentGithubId);

        List<Submission> findByTestId(AggregateReference<Test, Long> testId);

        List<Submission> findByQuestionId(AggregateReference<Question, Long> questionId);

        List<Submission> findByTestIdAndStudentGithubId(AggregateReference<Test, Long> testId, String studentGithubId);

        @Query("SELECT s.id, s.test_id, s.question_id, s.student_github_id, s.submitted_text, s.score, s.feedback " +
                        "FROM submissions s " +
                        "WHERE s.test_id = :testId AND s.question_id = :questionId AND s.student_github_id = :studentGithubId")
        Optional<Submission> findByTestIdAndQuestionIdAndStudentGithubId(
                        @Param("testId") Long testId,
                        @Param("questionId") Long questionId,
                        @Param("studentGithubId") String studentGithubId);

        List<Submission> findByQuestionIdAndScoreIsNull(AggregateReference<Question, Long> questionId);

        List<Submission> findByScoreIsNull();

}