package de.hhu.exambyte.service;

import java.util.List;
import java.util.Optional;

import de.hhu.exambyte.dto.SubmissionForCorrectionDto;

public interface CorrectionService {
    List<SubmissionForCorrectionDto> findUnscoredFreitextSubmissions();

    Optional<SubmissionForCorrectionDto> getSubmissionForCorrection(long submissionId);

    void saveGrade(long submissionId, double score, String feedback, String correctorId)
            throws IllegalArgumentException;
}