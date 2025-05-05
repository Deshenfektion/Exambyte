package de.hhu.exambyte.controller;

import de.hhu.exambyte.configuration.MethodSecurity;
import de.hhu.exambyte.helper.WithMockOAuth2User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import java.util.logging.MemoryHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Für CSRF-Schutz bei POST-Requests
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CorrectorController.class)
@Import(MethodSecurity.class)
public class CorrectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CorrectionService correctionService; // Service für Korrekturlogik

    @Test
    @WithMockUser(roles = "CORRECTOR") // Simuliert eingeloggte:n Korrektor:in
    @DisplayName("Korrektor kann das Korrektur-Dashboard sehen")
    void testCorrectorDashboardAccess() throws Exception {
        // Mock Service-Aufruf, um eine Liste von zu korrigierenden Aufgaben
        // zurückzugeben
        // when(correctionService.getSubmissionsForCorrector(any())).thenReturn(List.of());

        mockMvc.perform(get("/corrector/dashboard")) // Annahme: Endpunkt /corrector/dashboard
                .andExpect(status().isOk())
                .andExpect(view().name("corrector/dashboard"))
                .andExpect(model().attributeExists("submissionsToCorrect")); // Erwartetes Model-Attribut
    }

    @Test
    @WithMockUser(roles = { "STUDENT", "ORGANIZER" }) // Simuliert andere Rollen
    @DisplayName("Nicht-Korrektoren können nicht auf das Korrektur-Dashboard zugreifen")
    void testNonCorrectorDashboardAccess() throws Exception {
        mockMvc.perform(get("/corrector/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser // Simuliert anonymen Benutzer
    @DisplayName("Anonyme Benutzer können nicht auf das Korrektur-Dashboard zugreifen")
    void testAnonymousDashboardAccess() throws Exception {
        mockMvc.perform(get("/corrector/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/github"));
    }

    @Test
    @WithMockUser(roles = "CORRECTOR")
    @DisplayName("Korrektor kann eine Korrekturseite für eine Freitextaufgabe sehen")
    void testCorrectorGradeViewAccess() throws Exception {
        long submissionId = 1L;
        // Mock Service-Aufruf, um Details der Einreichung zurückzugeben
        // Submission submission = new Submission(submissionId, ... /* weitere Details
        // */);
        // when(correctionService.getSubmissionForCorrectionById(eq(submissionId),
        // any())).thenReturn(Optional.of(submission));

        mockMvc.perform(get("/corrector/submissions/{submissionId}", submissionId)) // Annahme: Endpunkt
                                                                                    // /corrector/submissions/{id}
                .andExpect(status().isOk())
                .andExpect(view().name("corrector/grade_submission"))
                .andExpect(model().attributeExists("submission")); // Erwartetes Model-Attribut
    }

    @Test
    @WithMockUser(roles = "CORRECTOR")
    @DisplayName("Korrektor kann eine Bewertung für eine Freitextaufgabe abschicken")
    void testCorrectorSubmitGrade() throws Exception {
        long submissionId = 1L;
        double score = 8.0;
        String feedback = "Gut gemacht!";

        mockMvc.perform(post("/corrector/submissions/{submissionId}", submissionId)
                .param("score", String.valueOf(score)) // Parameter für Punkte
                .param("feedback", feedback) // Parameter für Feedback
                .with(csrf())) // CSRF-Token hinzufügen (wichtig für POST)
                .andExpect(status().is3xxRedirection()) // Erwartet Weiterleitung nach erfolgreicher Bewertung
                .andExpect(redirectedUrl("/corrector/dashboard")); // Annahme: Weiterleitung zurück zum Dashboard

        // Überprüfen, ob der Service korrekt aufgerufen wurde
        // verify(correctionService).saveGrade(eq(submissionId), eq(score),
        // eq(feedback), any());
    }

    @Test
    @WithMockUser(roles = "CORRECTOR")
    @DisplayName("Korrektor muss Feedback geben, wenn nicht maximale Punktzahl vergeben wird")
    void testCorrectorSubmitGradeRequiresFeedback() throws Exception {
        long submissionId = 1L;
        double score = 5.0; // Nicht die maximale Punktzahl (Annahme)
        String feedback = ""; // Leeres Feedback

        // Annahme: Validierung im Controller oder Service fängt dies ab und gibt Fehler
        // zurück
        // Beispiel: Mocken, dass der Service eine Exception wirft oder false zurückgibt
        // doThrow(new IllegalArgumentException("Feedback
        // required")).when(correctionService).saveGrade(eq(submissionId), eq(score),
        // eq(feedback), any());
        // ODER: Der Controller validiert und fügt einen Fehler zum Model hinzu

        mockMvc.perform(post("/corrector/submissions/{submissionId}", submissionId)
                .param("score", String.valueOf(score))
                .param("feedback", feedback)
                .with(csrf()))
                .andExpect(status().isOk()) // Bleibt auf der Seite (oder leitet zurück zum Formular)
                .andExpect(view().name("corrector/grade_submission")) // Zeigt das Bewertungsformular erneut an
                .andExpect(model().attributeHasFieldErrors("gradeCommand", "feedback")); // Annahme: Es gibt ein
                                                                                         // Formular-Objekt
                                                                                         // 'gradeCommand' mit
                                                                                         // Validierungsfehlern für das
                                                                                         // Feld 'feedback'

        // Sicherstellen, dass der Service *nicht* erfolgreich mit ungültigen Daten
        // aufgerufen wurde
        // verify(correctionService, never()).saveGrade(eq(submissionId), eq(score),
        // eq(feedback), any());
    }
}