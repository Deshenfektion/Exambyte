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

import de.hhu.exambyte.domain.model.Submission;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.service.TestService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    private final TestService testService;

    public StudentController(TestService testService) {
        this.testService = testService;
    }

    // Helper Methode um die GitHub Login ID sicher zu extrahieren
    // Diese Methode MUSS angepasst werden, je nachdem welches Attribut
    // in deiner *aktuellen* Security Config den eindeutigen User kennzeichnet!
    // GitHub liefert oft 'login' (Username) oder 'id' (numerische ID).
    // Da du deine alte Config behältst, prüfe was dort verwendet wurde.
    // Im Beispiel von vorhin war es 'login'.
    private String getStudentId(OAuth2User principal) {
        if (principal == null) {
            log.error("OAuth2User principal is null!");
            return null; // Oder wirf eine Exception
        }
        // Prüfe, welches Attribut in deinem OAuth2User vorhanden und eindeutig ist.
        // Übliche Kandidaten: "login", "id", "sub"
        String studentId = principal.getAttribute("login");
        if (studentId == null) {
            log.error("Could not retrieve 'login' attribute from principal. Available attributes: {}",
                    principal.getAttributes());
            // Fallback oder Fehler werfen
            return null;
        }
        // Für die Speicherung in 'submission' ist evtl. ein Prefix sinnvoll,
        // um Nutzer von verschiedenen Providern (falls später erweitert) zu
        // unterscheiden
        // return "github|" + studentId; // Konsistent mit OrganizerControllerTest?
        // Checken!
        return studentId; // Oder einfach den Login, wenn nur GitHub verwendet wird
    }

    @GetMapping("/dashboard")
    @Secured("ROLE_STUDENT") // Stelle sicher, dass dies mit deiner Config übereinstimmt
    public String studentDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        String studentId = getStudentId(principal);
        if (studentId == null) {
            // Fehlerbehandlung: Nicht eingeloggt oder ID nicht gefunden
            return "redirect:/error?message=user_not_found";
        }
        log.info("Accessing student dashboard for user: {}", studentId);
        model.addAttribute("username", studentId); // Zeige den Namen/Login im Template an

        // Lade Tests für den Studenten (vereinfachte Version)
        List<Test> tests = testService.getTestsForStudent(studentId);
        model.addAttribute("tests", tests);

        // TODO: Lade Zulassungsstatus (später)
        // model.addAttribute("admissionStatus",
        // resultService.getAdmissionStatus(studentId));

        return "student/dashboard"; // Pfad zur Thymeleaf-Vorlage
    }

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
            // TODO: Bessere Fehlerseite oder Nachricht anzeigen
            model.addAttribute("errorMessage", "Test nicht gefunden oder nicht verfügbar.");
            return "student/dashboard"; // Zurück zum Dashboard mit Fehlermeldung
        }

        Test test = testOpt.get();
        boolean isReadOnly = !testService.isTestActive(testId); // Prüfen, ob Testzeitraum aktiv ist
        log.debug("Test {} is active: {}", testId, !isReadOnly);

        // Lade bereits vorhandene Einreichungen für diesen Test und Studenten
        List<Submission> submissionsList = testService.getSubmissionsForStudentTest(testId, studentId);
        // Wandle Liste in eine Map<QuestionID, SubmittedText> um für einfachen Zugriff
        // im Template
        Map<Long, String> submittedAnswers = submissionsList.stream()
                .filter(s -> s.submittedText() != null) // Nur Freitext für's Erste
                .collect(Collectors.toMap(
                        s -> s.questionId().getId(), // Schlüssel: Question ID
                        Submission::submittedText, // Wert: Eingereichter Text
                        (existing, replacement) -> replacement // Bei Duplikaten (sollte nicht vorkommen) nimm den neuen
                ));

        model.addAttribute("test", test);
        model.addAttribute("submittedAnswers", submittedAnswers); // Map für einfachen Zugriff auf Antworten
        model.addAttribute("isReadOnly", isReadOnly); // Flag für Template (Felder sperren?)

        return "student/test_view"; // Pfad zur Testansicht-Vorlage
    }

    @PostMapping("/tests/{testId}/submit")
    @Secured("ROLE_STUDENT")
    public String submitTest(
            @PathVariable long testId,
            // Empfängt alle Formularparameter. Wir extrahieren die Antworten daraus.
            // Der Key sollte "answers[QUESTION_ID]" sein, wie im Formular definiert.
            @RequestParam Map<String, String> allParams,
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes redirectAttributes) {

        String studentId = getStudentId(principal);
        if (studentId == null)
            return "redirect:/error?message=user_not_found";
        log.info("Student {} submitting answers for test {}", studentId, testId);

        try {
            // Iteriere durch die übermittelten Parameter und suche nach Antworten
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String paramName = entry.getKey();
                // Prüfe, ob der Parametername unserem Antwortformat entspricht (z.B.
                // "answers[123]")
                if (paramName.startsWith("answers[") && paramName.endsWith("]")) {
                    try {
                        // Extrahiere Question ID aus dem Parameternamen
                        long questionId = Long.parseLong(paramName.substring(8, paramName.length() - 1));
                        String submittedText = entry.getValue();

                        log.debug("Processing answer for question {}: '{}'", questionId, submittedText);
                        // Speichere die Antwort über den Service
                        testService.saveOrUpdateSubmission(testId, questionId, studentId, submittedText);

                    } catch (NumberFormatException e) {
                        log.warn("Could not parse question ID from parameter name: {}", paramName);
                        // Ignoriere diesen Parameter oder logge einen Fehler
                    } catch (IllegalStateException e) {
                        // Test ist nicht mehr aktiv -> Fehler an Benutzer
                        log.warn("Submission failed for test {}: {}", testId, e.getMessage());
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                        // Redirect zurück zur (jetzt read-only) Testansicht
                        return "redirect:/student/tests/" + testId;
                    }
                }
            }

            log.info("Successfully processed submissions for student {} in test {}", studentId, testId);
            redirectAttributes.addFlashAttribute("successMessage", "Deine Antworten wurden gespeichert!");
            // Leite zurück zum Dashboard oder zur Testansicht
            return "redirect:/student/dashboard";

        } catch (Exception e) {
            log.error("Error processing submission for student {} in test {}: {}", studentId, testId, e.getMessage(),
                    e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
            return "redirect:/student/tests/" + testId; // Zurück zur Testansicht
        }
    }
}
