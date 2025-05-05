import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import de.hhu.exambyte.configuration.MethodSecurity;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Zielcontroller anpassen, falls Logik im StudentController liegt
@WebMvcTest(TestExecutionController.class)
@Import(MethodSecurity.class)
public class TestExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestService testService; // Service für Testlogik (inkl. Durchführung)

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann während des Testzeitraums auf eine Frage zugreifen")
    void testStudentAccessQuestionDuringTest() throws Exception {
        long testId = 1L;
        long questionId = 10L;

        // Mock Service, um anzuzeigen, dass der Test aktiv ist und die Frage
        // zurückzugeben
        // when(testService.isTestActiveForStudent(eq(testId), any())).thenReturn(true);
        // Question question = new Question(questionId, "Fragentext...", ...);
        // when(testService.getQuestionForStudent(eq(testId), eq(questionId),
        // any())).thenReturn(Optional.of(question));

        // Annahme: Endpunkt zum Anzeigen einer spezifischen Frage innerhalb eines Tests
        mockMvc.perform(get("/student/tests/{testId}/questions/{questionId}", testId, questionId))
                .andExpect(status().isOk())
                .andExpect(view().name("student/question_view")) // Oder Teil einer größeren Testansicht (Fragment)
                .andExpect(model().attributeExists("question", "submission")); // Erwartet Frage und aktuelle
                                                                               // Einreichungsdaten
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann während des Testzeitraums eine Antwort abschicken")
    void testStudentSubmitAnswerDuringTest() throws Exception {
        long testId = 1L;
        long questionId = 10L; // ID der Frage, für die geantwortet wird

        // Mock Service, um anzuzeigen, dass der Test aktiv ist
        // when(testService.isTestActiveForStudent(eq(testId), any())).thenReturn(true);

        // Annahme: POST-Endpunkt zum Speichern von Antworten für den gesamten Test oder
        // pro Frage
        // Die Parameterstruktur hängt stark vom Form-Binding ab. Hier ein Beispiel:
        mockMvc.perform(post("/student/tests/{testId}/submit", testId) // Beispielhafter Endpunkt zum Speichern
                // Beispiel für Freitext: Der Name könnte dynamisch sein oder einer Map
                // entsprechen
                .param("answers[" + questionId + "].freetext", "Antwort des Studenten")
                // Beispiel für MC: Mehrere Werte für ausgewählte Optionen
                // .param("answers[" + mcQuestionId + "].selectedOptions", "option1", "option3")
                .with(csrf()))
                .andExpect(status().is3xxRedirection()); // Oder .isOk(), falls per AJAX gespeichert wird und man auf
                                                         // der Seite bleibt
        // .andExpect(redirectedUrl(...)); // Prüfen, wohin weitergeleitet wird (z.B.
        // Testübersicht, nächste Frage)

        // Überprüfen, ob der Service zum Speichern der Einreichung aufgerufen wurde
        // verify(testService).saveSubmission(eq(testId), any(),
        // any(SubmissionDto.class)); // Annahme: DTO wird verwendet
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann nach Testende keine Antwort mehr abschicken")
    void testStudentSubmitAnswerAfterTestEnd() throws Exception {
        long testId = 1L;
        long questionId = 10L;

        // Mock Service, um anzuzeigen, dass der Test *nicht* mehr aktiv ist
        // when(testService.isTestActiveForStudent(eq(testId),
        // any())).thenReturn(false);
        // ODER: Mocken, dass saveSubmission eine Exception wirft oder einen Fehler
        // zurückgibt
        // doThrow(new IllegalStateException("Test ist
        // vorbei")).when(testService).saveSubmission(anyLong(), any(), any());

        mockMvc.perform(post("/student/tests/{testId}/submit", testId)
                .param("answers[" + questionId + "].freetext", "Verspätete Antwort")
                .with(csrf()))
                .andExpect(status().isForbidden()); // Oder ein anderer geeigneter Status/View, der den Fehler anzeigt
    }

    @Test
    @WithMockUser(roles = "ORGANIZER") // Organisator darf keinen Test durchführen
    @DisplayName("Organisator kann nicht auf die Testdurchführungsseite zugreifen")
    void testOrganizerCannotTakeTest() throws Exception {
        long testId = 1L;
        long questionId = 10L;
        // GET-Request zur Frage sollte fehlschlagen
        mockMvc.perform(get("/student/tests/{testId}/questions/{questionId}", testId, questionId))
                .andExpect(status().isForbidden());

        // POST-Request zum Speichern sollte fehlschlagen
        mockMvc.perform(post("/student/tests/{testId}/submit", testId)
                .param("answers[" + questionId + "].freetext", "Organizer answer")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann nach Testende die Fragen navigieren (read-only)")
    void testStudentNavigateTestAfterEnd() throws Exception {
        long testId = 1L;
        long questionId = 10L;

        // Mock Service, um anzuzeigen, dass der Test beendet, aber anschaubar ist
        // when(testService.isTestFinishedAndReviewable(eq(testId),
        // any())).thenReturn(true); // Beispielhafte Methode
        // when(testService.isTestActiveForStudent(eq(testId),
        // any())).thenReturn(false);
        // Question question = new Question(questionId, "Fragentext...", ...);
        // when(testService.getQuestionForStudent(eq(testId), eq(questionId),
        // any())).thenReturn(Optional.of(question)); // Service gibt Frage auch nach
        // Ende zurück

        // Zugriff auf dieselbe URL wie während des Tests
        mockMvc.perform(get("/student/tests/{testId}/questions/{questionId}", testId, questionId))
                .andExpect(status().isOk())
                .andExpect(view().name("student/question_view")) // Potenziell dieselbe View
                // Wichtig: Überprüfen, ob die View einen Read-Only-Modus signalisiert
                .andExpect(model().attribute("isReadOnly", true))
                .andExpect(model().attributeExists("question", "submission")); // Zeigt Frage und gespeicherte Antwort
                                                                               // an
    }
}