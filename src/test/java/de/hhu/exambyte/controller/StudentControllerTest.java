package de.hhu.exambyte.controller;

import de.hhu.exambyte.configuration.MethodSecurity;
import de.hhu.exambyte.helper.WithMockOAuth2User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import(MethodSecurity.class)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestService testService;

    @MockBean
    private ResultService resultService;

    @Test
    @WithMockUser(roles = "STUDENT") // Simuliert einen eingeloggten Studenten
    @DisplayName("Student kann das Dashboard sehen")
    void testStudentDashboardAccess() throws Exception {
        // Beispielhafte Mock-Konfiguration für Service-Aufrufe
        // when(resultService.getAdmissionStatus(any())).thenReturn(AdmissionStatus.ON_TRACK);
        // when(testService.getTestsForStudent(any())).thenReturn(List.of());

        mockMvc.perform(get("/student/dashboard")) // Annahme: Endpunkt ist /student/dashboard
                .andExpect(status().isOk())
                .andExpect(view().name("student/dashboard"))
                .andExpect(model().attributeExists("tests", "admissionStatus")); // Überprüft, ob erwartete
                                                                                 // Model-Attribute vorhanden sind
    }

    @Test
    @WithMockUser(roles = { "ORGANIZER", "CORRECTOR" }) // Simuliert andere Rollen
    @DisplayName("Nicht-Studenten können nicht auf das Studenten-Dashboard zugreifen")
    void testNonStudentDashboardAccess() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isForbidden()); // Erwartet HTTP 403 Forbidden
    }

    @Test
    @WithAnonymousUser // Simuliert einen nicht eingeloggten Benutzer
    @DisplayName("Anonyme Benutzer werden zum Login weitergeleitet")
    void testAnonymousDashboardAccess() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection()) // Erwartet eine Weiterleitung (HTTP 3xx)
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/github")); // Überprüft, ob zur GitHub
                                                                                    // OAuth2-Seite weitergeleitet wird
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann die Testseite sehen (nach Start, vor Ende)")
    void testStudentTestViewAccess() throws Exception {
        long testId = 1L;
        // Mock Service-Aufrufe, um einen gültigen Test innerhalb des Zeitrahmens
        // zurückzugeben
        // Annahme: Es gibt ein Test-Objekt 'test'
        // Test test = new Test(testId, "Test 1", ...);
        // when(testService.getTestForStudent(eq(testId),
        // any())).thenReturn(Optional.of(test));
        // when(testService.isTestTimeActive(testId)).thenReturn(true); // Hilfsmethode,
        // um zu prüfen, ob der Test läuft

        mockMvc.perform(get("/student/tests/{testId}", testId)) // Annahme: Endpunkt /student/tests/{testId}
                .andExpect(status().isOk())
                .andExpect(view().name("student/test_view"))
                .andExpect(model().attributeExists("test")); // Überprüft, ob das Test-Objekt im Model ist
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann die Ergebnisseite sehen (nach Veröffentlichung)")
    void testStudentResultsViewAccess() throws Exception {
        long testId = 1L;
        // Mock Service-Aufrufe, um Ergebnisse für einen veröffentlichten Test
        // zurückzugeben
        // Annahme: Es gibt ein Result-Objekt 'result'
        // Result result = new Result(...);
        // when(resultService.getResultsForStudentIfPublished(eq(testId),
        // any())).thenReturn(Optional.of(result));

        mockMvc.perform(get("/student/results/{testId}", testId)) // Annahme: Endpunkt /student/results/{testId}
                .andExpect(status().isOk())
                .andExpect(view().name("student/results_view"))
                .andExpect(model().attributeExists("result")); // Überprüft, ob das Result-Objekt im Model ist
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann Ergebnisseite nicht vor Veröffentlichung sehen")
    void testStudentResultsViewAccessBeforePublication() throws Exception {
        long testId = 1L;
        // Mock Service-Aufrufe, um anzuzeigen, dass Ergebnisse noch nicht
        // veröffentlicht sind
        when(resultService.getResultsForStudentIfPublished(eq(testId), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/student/results/{testId}", testId))
                .andExpect(status().isForbidden()); // Oder Weiterleitung zum Dashboard mit Nachricht
    }
}