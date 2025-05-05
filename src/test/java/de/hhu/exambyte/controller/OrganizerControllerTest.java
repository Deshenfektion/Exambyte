package de.hhu.exambyte.controller;

import de.hhu.exambyte.configuration.MethodSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import de.hhu.exambyte.helper.WithMockOAuth2User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizerController.class)
@Import(MethodSecurity.class)
public class OrganizerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestService testService; // Service für Testlogik

    @MockBean
    private ResultService resultService; // Service für Ergebnislogik

    @MockBean
    private CorrectionService correctionService; // Service für Korrekturlogik (auch für Status)

    @MockBean
    private CsvExportService csvExportService; // Service für CSV-Export

    @Test
    @WithMockUser(roles = "ORGANIZER") // Simuliert eingeloggte:n Organisator:in
    @DisplayName("Organisator kann das Organizer-Dashboard sehen")
    void testOrganizerDashboardAccess() throws Exception {
        mockMvc.perform(get("/organizer/dashboard")) // Annahme: Endpunkt /organizer/dashboard
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/dashboard"));
    }

    @Test
    @WithMockUser(roles = { "STUDENT", "CORRECTOR" }) // Simuliert andere Rollen
    @DisplayName("Nicht-Organisatoren können nicht auf das Organizer-Dashboard zugreifen")
    void testNonOrganizerDashboardAccess() throws Exception {
        mockMvc.perform(get("/organizer/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser // Simuliert anonymen Benutzer
    @DisplayName("Anonyme Benutzer können nicht auf das Organizer-Dashboard zugreifen")
    void testAnonymousOrganizerDashboardAccess() throws Exception {
        mockMvc.perform(get("/organizer/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/github"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann die Testerstellungsseite sehen")
    void testOrganizerCreateTestViewAccess() throws Exception {
        mockMvc.perform(get("/organizer/tests/new")) // Annahme: Endpunkt zum Erstellen eines neuen Tests
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/create_test"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann einen neuen Test erstellen")
    void testOrganizerCreateTestSubmit() throws Exception {
        mockMvc.perform(post("/organizer/tests") // Annahme: POST-Endpunkt zum Speichern des neuen Tests
                .param("title", "Test 1")
                .param("startTime", "2024-01-01T10:00:00") // Zeitpunkte im ISO-Format
                .param("endTime", "2024-01-01T11:00:00")
                .param("publishTime", "2024-01-08T10:00:00")
                // Parameter für Fragen hinzufügen, abhängig von der Formularstruktur
                // z.B. .param("questions[0].text", "Frage 1?")
                // .param("questions[0].type", "MC") ... etc.
                .with(csrf()))
                .andExpect(status().is3xxRedirection()); // Erwartet Weiterleitung nach Erfolg
        // .andExpect(redirectedUrl("/organizer/tests")); // Ziel der Weiterleitung
        // prüfen

        // Überprüfen, ob der Service zum Erstellen des Tests aufgerufen wurde
        // verify(testService).createTest(any(TestCreateCommand.class)); // Annahme: Ein
        // Command-Objekt wird verwendet
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann Testvorschau sehen")
    void testOrganizerTestPreview() throws Exception {
        long testId = 1L;
        // Mock Service, um Testdetails für die Vorschau zurückzugeben
        // Test test = new Test(testId, "Test Preview", ...);
        // when(testService.getTestForPreview(testId)).thenReturn(Optional.of(test));

        mockMvc.perform(get("/organizer/tests/{testId}/preview", testId)) // Annahme: Endpunkt für Vorschau
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/test_preview"))
                .andExpect(model().attributeExists("test"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann die Korrekturstatusübersicht sehen")
    void testOrganizerCorrectionStatusView() throws Exception {
        // Mock Service, um Daten für die Statusübersicht zurückzugeben
        // CorrectionStatusOverview statusData = new CorrectionStatusOverview(...);
        // when(correctionService.getCorrectionStatusOverview()).thenReturn(statusData);

        mockMvc.perform(get("/organizer/correction-status")) // Annahme: Endpunkt für Korrekturstatus
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/correction_status"))
                .andExpect(model().attributeExists("statusOverview"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann die Ergebnisübersicht für einen Test sehen")
    void testOrganizerResultsOverview() throws Exception {
        long testId = 1L;
        // Mock Service, um Ergebnisdaten für den Test zurückzugeben
        // ResultsOverview resultsData = new ResultsOverview(...);
        // when(resultService.getResultsOverviewForTest(testId)).thenReturn(resultsData);

        mockMvc.perform(get("/organizer/tests/{testId}/results", testId)) // Annahme: Endpunkt für Testergebnisse
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/results_overview"))
                .andExpect(model().attributeExists("results"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann einen Test für einen Studenten als nicht bestanden markieren")
    void testOrganizerFailStudentTest() throws Exception {
        long testId = 1L;
        String studentGithubId = "github|12345"; // Annahme: Eindeutige ID des Studenten (z.B. von GitHub)
        String reason = "Plausibility check failed";

        mockMvc.perform(post("/organizer/tests/{testId}/fail/{studentId}", testId, studentGithubId) // Annahme: Endpunkt
                                                                                                    // zum
                                                                                                    // Durchfallenlassen
                .param("reason", reason) // Begründung als Parameter
                .with(csrf()))
                .andExpect(status().is3xxRedirection()); // Erwartet Weiterleitung
        // .andExpect(redirectedUrl(...)); // Ziel der Weiterleitung prüfen (z.B. zurück
        // zur Ergebnisübersicht)

        // Überprüfen, ob der Service korrekt aufgerufen wurde
        // verify(resultService).failTestForStudent(eq(testId), eq(studentGithubId),
        // eq(reason));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann Ergebnisse als CSV exportieren")
    void testOrganizerExportResultsCsv() throws Exception {
        long testId = 1L;
        String csvData = "UserId,Score,Passed\nstudent1,80.0,true\ngithub|12345,45.5,false";
        byte[] csvBytes = csvData.getBytes(StandardCharsets.UTF_8);

        // Mock Service, um die CSV-Daten als Bytes zurückzugeben
        when(csvExportService.generateResultsCsv(testId)).thenReturn(csvBytes);

        mockMvc.perform(get("/organizer/tests/{testId}/results/export", testId)) // Annahme: Endpunkt für CSV-Export
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv")) // Erwartet korrekten Content-Type
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"results_test_" + testId + ".csv\"")) // Erwartet korrekten Header für
                                                                                     // Dateidownload
                .andExpect(content().bytes(csvBytes)); // Vergleicht den Inhalt der Antwort mit den erwarteten Bytes
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann die Übersicht aller Freitextantworten für eine Frage sehen")
    void testOrganizerFreeTextAnswersOverview() throws Exception {
        long questionId = 5L; // ID der Freitextfrage
        // Mock Service, um alle Einreichungen für diese Frage zurückzugeben
        // Submission submission1 = new Submission(...);
        // Submission submission2 = new Submission(...);
        // when(correctionService.getAllSubmissionsForFreeTextQuestion(questionId)).thenReturn(List.of(submission1,
        // submission2));

        mockMvc.perform(get("/organizer/questions/{questionId}/submissions", questionId)) // Annahme: Endpunkt für alle
                                                                                          // Antworten zu einer Frage
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/freetext_submissions"))
                .andExpect(model().attributeExists("submissions")); // Erwartet Liste der Einreichungen im Model
    }
}