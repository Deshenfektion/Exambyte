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
@Secured("ROLE_CORRECTOR") // Sicherstellen, dass nur Korrektoren Zugriff haben
public class CorrectorController {

    private static final Logger log = LoggerFactory.getLogger(CorrectorController.class);
    private final CorrectionService correctionService;

    public CorrectorController(CorrectionService correctionService) {
        this.correctionService = correctionService;
    }

    // Helper für Corrector ID (optional, aber gut für Logs)
    private String getCorrectorId(OAuth2User principal) {
        if (principal == null)
            return "UNKNOWN_CORRECTOR";
        // Passe dies an, wie du User identifizierst ('login' oder 'id')
        return principal.getAttribute("login");
    }

    @GetMapping("/dashboard")
    public String correctorDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        String correctorId = getCorrectorId(principal);
        log.info("Corrector {} accessing dashboard", correctorId);

        List<SubmissionForCorrectionDto> submissions = correctionService.findUnscoredFreitextSubmissions();
        model.addAttribute("submissions", submissions);
        model.addAttribute("username", correctorId); // Für Anzeige im Template

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
            // TODO: Bessere Fehlerseite oder Redirect mit Flash Attribut
            return "corrector/dashboard"; // Zurück zum Dashboard
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
            return "redirect:/corrector/dashboard"; // Zurück zum Dashboard

        } catch (IllegalArgumentException | NoSuchElementException e) {
            log.warn("Failed to save grade for submission {}: {}", submissionId, e.getMessage());
            // Fehlermeldung zurück zum Formular geben
            redirectAttributes.addFlashAttribute("errorMessage", "Speichern fehlgeschlagen: " + e.getMessage());
            // Wichtig: Parameter für das Formular wieder hinzufügen, damit der Nutzer es
            // korrigieren kann
            // (Besser wäre es, das Form-Objekt direkt im Model zu behalten, aber für den
            // Anfang reicht Redirect)
            redirectAttributes.addFlashAttribute("submittedScore", score);
            redirectAttributes.addFlashAttribute("submittedFeedback", feedback);
            return "redirect:/corrector/submissions/" + submissionId; // Zurück zum Formular der Submission
        } catch (Exception e) {
            log.error("Unexpected error grading submission {}: {}", submissionId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
            return "redirect:/corrector/dashboard";
        }
    }
}
