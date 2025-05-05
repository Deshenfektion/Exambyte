package de.hhu.exambyte.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Für Flash Messages

import de.hhu.exambyte.domain.model.AnswerOption;
import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.QuestionType;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.service.TestService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    private static final Logger log = LoggerFactory.getLogger(OrganizerController.class);
    private final TestService testService;

    // Konstruktor-Injektion ist bevorzugt
    public OrganizerController(TestService testService) {
        this.testService = testService;
    }

    // --- Dashboard (Beispiel, falls noch nicht vorhanden) ---
    @GetMapping("/dashboard")
    @Secured("ROLE_ORGANIZER")
    public String organizerDashboard(Model model) {
        log.info("Accessing organizer dashboard");
        // Hier könnten Daten für das Dashboard geladen werden (z.B. Liste der Tests)
        // model.addAttribute("tests", testRepository.findAll()); // Beispiel
        return "organizer/dashboard"; // Pfad zum Organizer-Dashboard-Template
    }

    // --- Test erstellen ---

    /**
     * Zeigt das Formular zum Erstellen eines neuen Tests an.
     */
    @GetMapping("/tests/new")
    @Secured("ROLE_ORGANIZER")
    public String showCreateTestForm(Model model) {
        log.info("Showing create test form");
        // Optional: Ein leeres Objekt für das Form-Binding vorbereiten
        // model.addAttribute("testCommand", new TestCreateCommand());
        return "organizer/create_test"; // Pfad zum Template
    }

    /**
     * Verarbeitet die Übermittlung des Formulars zum Erstellen eines neuen Tests.
     * Vereinfachte Version: Nimmt erstmal nur *eine* Freitextfrage entgegen.
     */
    @PostMapping("/tests")
    @Secured("ROLE_ORGANIZER")
    public String handleCreateTest(
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishTime,
            // Parameter für die (erste) Frage
            @RequestParam String ft_questionText,
            @RequestParam double ft_maxPoints,
            // @RequestParam String solutionProposal, // Vorerst weggelassen
            @RequestParam String mc_questionText,
            @RequestParam double mc_maxPoints,
            // 4 MC Antwort Optionen (einfachheitshalber)
            @RequestParam String mc_option1_text,
            @RequestParam(required = false) boolean mc_option1_correct, // Checkbox sendet nur 'on' wenn checked, oder
                                                                        // gar nichts
            @RequestParam String mc_option2_text,
            @RequestParam(required = false) boolean mc_option2_correct,
            @RequestParam String mc_option3_text,
            @RequestParam(required = false) boolean mc_option3_correct,
            @RequestParam String mc_option4_text,
            @RequestParam(required = false) boolean mc_option4_correct,
            RedirectAttributes redirectAttributes // Um Erfolgs-/Fehlermeldungen anzuzeigen
    ) {
        log.info("Handling create test submission for title: {}", title);

        try {
            Set<Question> questions = new HashSet<>();

            // 1. Freitextfrage erstellen (wie bisher)
            Question freetextQuestion = new Question(
                    ft_questionText, ft_maxPoints, QuestionType.FREETEXT, null, new HashSet<>());
            questions.add(freetextQuestion);

            // 2. MC-Frage und Optionen erstellen
            Set<AnswerOption> mcOptions = new HashSet<>();
            mcOptions.add(new AnswerOption(mc_option1_text, mc_option1_correct));
            mcOptions.add(new AnswerOption(mc_option2_text, mc_option2_correct));
            mcOptions.add(new AnswerOption(mc_option3_text, mc_option3_correct));
            mcOptions.add(new AnswerOption(mc_option4_text, mc_option4_correct));

            Question mcQuestion = new Question(
                    mc_questionText, mc_maxPoints, QuestionType.MC, null, // Kein Lösungsvorschlag für MC hier
                    mcOptions // Die erstellten Optionen
            );
            questions.add(mcQuestion);

            // 3. Test-Objekt erstellen mit beiden Fragen
            Test testToCreate = new Test(
                    title, startTime, endTime, publishTime, questions);

            // 4. Test über den Service speichern
            Test createdTest = testService.createTest(testToCreate);
            log.info("Successfully created test with ID: {}", createdTest.id());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Test '" + createdTest.title() + "' erfolgreich erstellt!");
            return "redirect:/organizer/dashboard";

        } catch (Exception e) {
            log.error("Error creating test: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Erstellen des Tests: " + e.getMessage());
            return "redirect:/organizer/tests/new"; // Zurück zum Formular
        }
    }

    // --- Andere Organizer-Methoden (Übersichten etc.) ---
    // ... hier folgen später die Methoden für Ergebnisübersicht, Korrekturstatus
    // etc.
}
