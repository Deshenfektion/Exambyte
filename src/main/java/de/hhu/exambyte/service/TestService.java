package de.hhu.exambyte.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.domain.model.Test;

public interface TestService {
    Test createTest(Test test);

    Optional<Test> findTestForPreview(long testId);

    Optional<Test> findTestForStudent(long testId, String studentGithubId);

    List<Test> getTestsForStudent(String studentGithubId);

    boolean isTestActive(long testId);

    Submission saveOrUpdateSubmission(long testId, long questionId, String studentGithubId, String submittedText,
            Set<Long> selectedOptionIds);

    List<Submission> getSubmissionsForStudentTest(long testId, String studentGithubId);
}
