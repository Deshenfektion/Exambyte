package de.hhu.exambyte.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.domain.model.SubmissionSelectedOptionRef;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.repository.SubmissionRepository;
import de.hhu.exambyte.repository.TestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TestServiceImpl implements TestService {

    private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

    private final TestRepository testRepository;
    private final SubmissionRepository submissionRepository;

    public TestServiceImpl(TestRepository testRepository, SubmissionRepository submissionRepository) {
        this.testRepository = testRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional
    public Test createTest(Test test) {
        log.info("Creating new test: {}", test.title());
        return testRepository.save(test);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Test> findTestForPreview(long testId) {
        log.debug("Finding test for preview: {}", testId);
        return testRepository.findById(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Test> findTestForStudent(long testId, String studentGithubId) {
        log.debug("Finding test {} for student {}", testId, studentGithubId);
        Optional<Test> testOpt = testRepository.findById(testId);
        return testOpt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Test> getTestsForStudent(String studentGithubId) {
        log.debug("Getting all tests for student {}", studentGithubId);
        return (List<Test>) testRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTestActive(long testId) {
        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isPresent()) {
            Test test = testOpt.get();
            LocalDateTime now = LocalDateTime.now();
            boolean isActive = now.isAfter(test.startTime()) && now.isBefore(test.endTime());
            log.trace("Test {} active check: Start={}, End={}, Now={}, Active={}",
                    testId, test.startTime(), test.endTime(), now, isActive);
            return isActive;
        }
        log.warn("isTestActive check failed: Test {} not found", testId);
        return false;
    }

    @Override
    @Transactional
    public Submission saveOrUpdateSubmission(long testId, long questionId, String studentGithubId,
            String submittedText, Set<Long> selectedOptionIds) {

        log.debug("Saving submission for test {}, question {}, student {}: Text='{}', Options={}",
                testId, questionId, studentGithubId, submittedText, selectedOptionIds);

        if (!isTestActive(testId)) {
            log.error("Submission attempt failed: Test {} is not active.", testId);
            throw new IllegalStateException("Der Test ist derzeit nicht bearbeitbar.");
        }

        Optional<Submission> existingSubmissionOpt = submissionRepository.findByTestIdAndQuestionIdAndStudentGithubId(
                testId, questionId, studentGithubId);

        final Set<SubmissionSelectedOptionRef> selectedOptionRefs = Submission.longsToRefs(selectedOptionIds);

        Submission submissionToSave;
        if (existingSubmissionOpt.isPresent()) {
            Submission existing = existingSubmissionOpt.get();
            log.trace("Found existing submission with ID: {}", existing.id());

            if (submittedText != null) {
                submissionToSave = existing.updateFreeText(submittedText);
                log.trace("Updating existing freetext submission {}", existing.id());
            } else {
                submissionToSave = existing.updateSelectedOptions(selectedOptionRefs);
                log.trace("Updating existing MC submission {} with options {}", existing.id(), selectedOptionIds);
            }
        } else {
            AggregateReference<Test, Long> testRefForNew = AggregateReference.to(testId);
            AggregateReference<Question, Long> questionRefForNew = AggregateReference.to(questionId);
            log.trace("No existing submission found... Creating new one.");

            if (submittedText != null) {
                submissionToSave = new Submission(testRefForNew, questionRefForNew, studentGithubId, submittedText);
                log.trace("Creating new freetext submission.");
            } else {
                submissionToSave = new Submission(testRefForNew, questionRefForNew, studentGithubId,
                        selectedOptionRefs);
                log.trace("Creating new MC submission with options {}.", selectedOptionIds);
            }
        }

        log.debug("Saving submission: {}", submissionToSave);
        Submission savedSubmission = submissionRepository.save(submissionToSave);
        log.info("Successfully saved submission with ID: {}", savedSubmission.id());
        return savedSubmission;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsForStudentTest(long testId, String studentGithubId) {
        log.debug("Getting submissions for student {} in test {}", studentGithubId, testId);
        List<Submission> submissions = submissionRepository.findByTestIdAndStudentGithubId(testId, studentGithubId);
        log.debug("Found {} submissions for student {} in test {}", submissions.size(), studentGithubId, testId);
        return submissions;
    }
}