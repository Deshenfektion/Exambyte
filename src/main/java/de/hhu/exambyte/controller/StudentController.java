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
import org.springframework.data.jdbc.core.mapping.AggregateReference; // Importieren

import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.domain.model.SubmissionSelectedOptionRef;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.service.TestService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap; // Importieren
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set; // Importieren
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    private final TestService testService;

    public StudentController(TestService testService) {
        this.testService = testService;
    }

    // --- getStudentId Methode bleibt unverändert ---
    private String getStudentId(OAuth2User principal) {
        log.error("getStudentId called. Principal: {}", principal); // NEUES LOG

        if (principal == null) {
            log.error("OAuth2User principal is null!");
            return null;
        }
        String studentId = principal.getAttribute("login");
        log.error("Attribute 'login' from principal: {}", studentId); // NEUES LOG

        if (studentId == null) {
            log.error("Could not retrieve 'login' attribute from principal. Available attributes: {}",
                    principal.getAttributes());
            return null;
        }
        return studentId;
    }

    // --- studentDashboard Methode bleibt unverändert ---
    @GetMapping("/dashboard")
    @Secured("ROLE_STUDENT")
    public String studentDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        String studentId = getStudentId(principal);
        if (studentId == null) {
            return "redirect:/error?message=user_not_found";
        }
        log.info("Accessing student dashboard for user: {}", studentId);
        model.addAttribute("username", studentId);

        List<Test> tests = testService.getTestsForStudent(studentId);
        model.addAttribute("tests", tests);
        return "student/dashboard";
    }

    // --- viewTest Methode ANPASSEN ---
    @GetMapping("/tests/{testId}")
    @Secured("ROLE_STUDENT")
    public String viewTest(@PathVariable long testId, Model model, @AuthenticationPrincipal OAuth2User principal) {
        String studentId = getStudentId(principal);
        if (studentId == null)
            return "redirect:/error?message=user_not_found";
        log.info("Student {} viewing test {}", studentId, testId);

        Optional<Test> testOpt = testService.findTestForStudent(testId, studentId);

        if (testOpt.isEmpty()) {
            log.warn("Test {} not found or not accessible for student {}", testId, studentId);
            model.addAttribute("errorMessage", "Test nicht gefunden oder nicht verfügbar.");
            // Leite zum Dashboard weiter, wenn der Test nicht gefunden wurde
            // oder gib eine spezielle Fehlerseite zurück.
            return "redirect:/student/dashboard?error=Test+nicht+gefunden";
        }

        Test test = testOpt.get();
        boolean isReadOnly = !testService.isTestActive(testId);
        log.debug("Test {} is active: {}", testId, !isReadOnly);

        // Lade bereits vorhandene Einreichungen für diesen Test und Studenten
        List<Submission> submissionsList = testService.getSubmissionsForStudentTest(testId, studentId);

        Map<Long, String> submittedFreetextAnswers = new HashMap<>();
        Map<Long, Set<Long>> selectedOptionIdsMap = new HashMap<>(); // Zielformat bleibt gleich

        for (Submission submission : submissionsList) {
            Long qId = submission.questionId().getId();

            if (submission.submittedText() != null) {
                submittedFreetextAnswers.put(qId, submission.submittedText());
            }
            // *** GEÄNDERT: Konvertierung von Set<SubmissionSelectedOptionRef> zu Set<Long>
            // ***
            else if (submission.selectedOptions() != null && !submission.selectedOptions().isEmpty()) {
                // Extrahiere die Long IDs aus den SubmissionSelectedOptionRef-Objekten
                Set<Long> optionIds = submission.selectedOptions().stream()
                        .map(SubmissionSelectedOptionRef::answerOptionId) // Hole die Long ID
                        .collect(Collectors.toSet());
                selectedOptionIdsMap.put(qId, optionIds);
            }
        }
        log.debug("Prepared freetext answers map: {}", submittedFreetextAnswers);
        log.debug("Prepared selected MC options map for template: {}", selectedOptionIdsMap);

        model.addAttribute("test", test);
        model.addAttribute("submittedFreetextAnswers", submittedFreetextAnswers);
        model.addAttribute("selectedOptionIdsMap", selectedOptionIdsMap); // Hier bleibt es Set<Long> für das Template
        model.addAttribute("isReadOnly", isReadOnly);

        return "student/test_view"; // Name deiner Thymeleaf-Vorlage
    }

    // In StudentController.java

    @PostMapping("/tests/{testId}/submit")
    @Secured("ROLE_STUDENT")
    public String submitTest(
            @PathVariable long testId,
            // Wir verwenden HttpServletRequest, um alle Parameterwerte zu bekommen,
            // insbesondere für MC-Fragen mit Mehrfachauswahl.
            HttpServletRequest request, // <-- WICHTIGE ÄNDERUNG!
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes redirectAttributes) {

        log.error("!!!!!!!!!!! SUBMITTEST METHOD ENTERED (Test ID: {}) !!!!!!!!!!!", testId);
        // Logge die Parameter direkt aus dem Request, um zu sehen, was ankommt
        request.getParameterMap()
                .forEach((key, value) -> log.debug("Request Param: {} = {}", key, Arrays.toString(value)));

        String studentId = getStudentId(principal);
        if (studentId == null) {
            log.error("StudentId is null, redirecting to error page.");
            return "redirect:/error?message=user_not_found";
        }
        log.info("Student {} submitting answers for test {}", studentId, testId);

        boolean isActive = testService.isTestActive(testId);
        if (!isActive) {
            log.warn("Submission attempt failed for test {}: Test is no longer active.", testId);
            redirectAttributes.addFlashAttribute("errorMessage", "Der Test ist nicht mehr bearbeitbar.");
            return "redirect:/student/tests/" + testId;
        }

        try {
            // Sammle alle Question-IDs, die im Request vorkommen (sowohl Freetext als auch
            // MC)
            Set<Long> questionIdsFromRequest = new HashSet<>();
            for (String paramName : Collections.list(request.getParameterNames())) {
                if (paramName.startsWith("answersFreetext[") && paramName.endsWith("]")) {
                    try {
                        String idStr = paramName.substring("answersFreetext[".length(), paramName.length() - 1);
                        questionIdsFromRequest.add(Long.parseLong(idStr));
                    } catch (NumberFormatException e) {
                        /* ignoriere ungültige */ }
                } else if (paramName.startsWith("answersMC[") && paramName.endsWith("]")) {
                    try {
                        String idStr = paramName.substring("answersMC[".length(), paramName.length() - 1);
                        questionIdsFromRequest.add(Long.parseLong(idStr));
                    } catch (NumberFormatException e) {
                        /* ignoriere ungültige */ }
                }
            }
            log.debug("Found question IDs in request to process: {}", questionIdsFromRequest);

            // Iteriere über die gefundenen Question-IDs und verarbeite sie
            for (Long questionId : questionIdsFromRequest) {
                String submittedText = null;
                Set<Long> selectedOptionIds = null;

                // Versuche, Freitext für diese questionId zu bekommen
                String freetextParamName = "answersFreetext[" + questionId + "]";
                String[] freetextValues = request.getParameterValues(freetextParamName);
                if (freetextValues != null && freetextValues.length > 0) {
                    // Nimm den ersten Wert (sollte nur einer sein für Textarea)
                    submittedText = freetextValues[0];
                    log.debug("Processing FreeText for question {}: '{}'", questionId, submittedText);
                }

                // Versuche, MC-Optionen für diese questionId zu bekommen
                String mcParamName = "answersMC[" + questionId + "]";
                String[] mcOptionValues = request.getParameterValues(mcParamName); // Gibt alle Werte für diesen Namen
                                                                                   // zurück

                if (mcOptionValues != null && mcOptionValues.length > 0) {
                    selectedOptionIds = new HashSet<>();
                    for (String optionIdStr : mcOptionValues) {
                        try {
                            selectedOptionIds.add(Long.parseLong(optionIdStr));
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse option ID '{}' for MC question {}", optionIdStr, questionId);
                        }
                    }
                    log.debug("Processing MC for question {}: Options {}", questionId, selectedOptionIds);
                }

                // Speichere die Submission, wenn Text oder Optionen vorhanden sind
                // Oder auch, wenn keine MC-Optionen gewählt wurden (leeres Set speichern)
                if (submittedText != null || selectedOptionIds != null) {
                    testService.saveOrUpdateSubmission(testId, questionId, studentId, submittedText,
                            selectedOptionIds != null ? selectedOptionIds : Collections.emptySet()); // Stelle sicher,
                                                                                                     // dass Set nicht
                                                                                                     // null ist
                } else if (mcOptionValues != null) { // Es war eine MC-Frage, aber nichts ausgewählt
                    log.debug("MC question {} had no options selected. Saving empty selection.", questionId);
                    testService.saveOrUpdateSubmission(testId, questionId, studentId, null, Collections.emptySet());
                }
            }

            log.info("Successfully processed submissions for student {} in test {}", studentId, testId);
            redirectAttributes.addFlashAttribute("successMessage", "Deine Antworten wurden gespeichert!");
            return "redirect:/student/dashboard";

        } catch (Exception e) {
            log.error("Error processing submission for student {} in test {}: {}", studentId, testId, e.getMessage(),
                    e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ein unerwarteter Fehler beim Speichern ist aufgetreten.");
            return "redirect:/student/tests/" + testId;
        }
    }
}