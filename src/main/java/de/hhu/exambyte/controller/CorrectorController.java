package de.hhu.exambyte.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.hhu.exambyte.dto.SubmissionForCorrectionDto;
import de.hhu.exambyte.service.CorrectionService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
@RequestMapping("/corrector")
@Secured("ROLE_CORRECTOR")
public class CorrectorController {

    private static final Logger log = LoggerFactory.getLogger(CorrectorController.class);
    private final CorrectionService correctionService;

    public CorrectorController(CorrectionService correctionService) {
        this.correctionService = correctionService;
    }

    private String getCorrectorId(OAuth2User principal) {
        if (principal == null)
            return "UNKNOWN_CORRECTOR";
        return principal.getAttribute("login");
    }

    @GetMapping("/dashboard")
    public String correctorDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        String correctorId = getCorrectorId(principal);
        log.info("Corrector {} accessing dashboard", correctorId);

        List<SubmissionForCorrectionDto> submissions = correctionService.findUnscoredFreitextSubmissions();
        model.addAttribute("submissions", submissions);
        model.addAttribute("username", correctorId);

        return "corrector/dashboard";
    }

    @GetMapping("/submissions/{submissionId}")
    public String showGradeSubmissionForm(@PathVariable long submissionId, Model model,
            @AuthenticationPrincipal OAuth2User principal) {
        String correctorId = getCorrectorId(principal);
        log.info("Corrector {} viewing submission {}", correctorId, submissionId);

        Optional<SubmissionForCorrectionDto> submissionDtoOpt = correctionService
                .getSubmissionForCorrection(submissionId);

        if (submissionDtoOpt.isEmpty()) {
            log.warn("Submission {} not found for correction.", submissionId);
            model.addAttribute("errorMessage", "Einreichung nicht gefunden.");
            return "corrector/dashboard";
        }

        model.addAttribute("submissionDto", submissionDtoOpt.get());
        model.addAttribute("username", correctorId);
        return "corrector/grade_submission";
    }

    @PostMapping("/submissions/{submissionId}")
    public String handleGradeSubmission(
            @PathVariable long submissionId,
            @RequestParam double score,
            @RequestParam String feedback,
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes redirectAttributes) {

        String correctorId = getCorrectorId(principal);
        log.info("Corrector {} submitting grade for submission {}", correctorId, submissionId);

        try {
            correctionService.saveGrade(submissionId, score, feedback, correctorId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Bewertung für Einreichung " + submissionId + " erfolgreich gespeichert.");
            return "redirect:/corrector/dashboard";

        } catch (IllegalArgumentException | NoSuchElementException e) {
            log.warn("Failed to save grade for submission {}: {}", submissionId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Speichern fehlgeschlagen: " + e.getMessage());
            redirectAttributes.addFlashAttribute("submittedScore", score);
            redirectAttributes.addFlashAttribute("submittedFeedback", feedback);
            return "redirect:/corrector/submissions/" + submissionId;
        } catch (Exception e) {
            log.error("Unexpected error grading submission {}: {}", submissionId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
            return "redirect:/corrector/dashboard";
        }
    }
}
