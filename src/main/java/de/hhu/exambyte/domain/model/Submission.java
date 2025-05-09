package de.hhu.exambyte.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Table("submissions")
public record Submission(
        @Id Long id,
        AggregateReference<Test, Long> testId,
        AggregateReference<Question, Long> questionId,
        String studentGithubId,
        String submittedText,
        @MappedCollection(idColumn = "submission_id", keyColumn = "answer_option_id") Set<SubmissionSelectedOptionRef> selectedOptions,
        Double score,
        String feedback) {

    public Submission(AggregateReference<Test, Long> testId, AggregateReference<Question, Long> questionId,
            String studentGithubId, String submittedText) {
        this(null, testId, questionId, studentGithubId, submittedText, Collections.emptySet(), null, null);
    }

    public Submission(AggregateReference<Test, Long> testId, AggregateReference<Question, Long> questionId,
            String studentGithubId, Set<SubmissionSelectedOptionRef> selectedOptionsRefs) {
        this(null, testId, questionId, studentGithubId, null,
                selectedOptionsRefs != null ? selectedOptionsRefs : Collections.emptySet(), null, null);
    }

    public Submission withGrade(double score, String feedback) {
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, this.submittedText,
                this.selectedOptions, score, feedback);
    }

    public Submission updateFreeText(String newText) {
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, newText,
                Collections.emptySet(), this.score, this.feedback);
    }

    public Submission updateSelectedOptions(Set<SubmissionSelectedOptionRef> newOptions) {
        return new Submission(this.id, this.testId, this.questionId, this.studentGithubId, null,
                newOptions != null ? newOptions : Collections.emptySet(), this.score, this.feedback);
    }

    public static Set<SubmissionSelectedOptionRef> longsToRefs(Set<Long> optionIds) {
        if (optionIds == null)
            return Collections.emptySet();
        return optionIds.stream().map(SubmissionSelectedOptionRef::new).collect(Collectors.toSet());
    }
}