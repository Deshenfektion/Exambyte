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
    private TestService testService;

    @MockBean
    private ResultService resultService;

    @MockBean
    private CorrectionService correctionService;

    @MockBean
    private CsvExportService csvExportService;

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann das Organizer-Dashboard sehen")
    void testOrganizerDashboardAccess() throws Exception {
        mockMvc.perform(get("/organizer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/dashboard"));
    }

    @Test
    @WithMockUser(roles = { "STUDENT", "CORRECTOR" })
    @DisplayName("Nicht-Organisatoren können nicht auf das Organizer-Dashboard zugreifen")
    void testNonOrganizerDashboardAccess() throws Exception {
        mockMvc.perform(get("/organizer/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
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
        mockMvc.perform(get("/organizer/tests/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/create_test"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann einen neuen Test erstellen")
    void testOrganizerCreateTestSubmit() throws Exception {
        mockMvc.perform(post("/organizer/tests")
                .param("title", "Test 1")
                .param("startTime", "2024-01-01T10:00:00")
                .param("endTime", "2024-01-01T11:00:00")
                .param("publishTime", "2024-01-08T10:00:00")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann Testvorschau sehen")
    void testOrganizerTestPreview() throws Exception {
        long testId = 1L;

        mockMvc.perform(get("/organizer/tests/{testId}/preview", testId))
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/test_preview"))
                .andExpect(model().attributeExists("test"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann die Korrekturstatusübersicht sehen")
    void testOrganizerCorrectionStatusView() throws Exception {
        mockMvc.perform(get("/organizer/correction-status"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/correction_status"))
                .andExpect(model().attributeExists("statusOverview"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann die Ergebnisübersicht für einen Test sehen")
    void testOrganizerResultsOverview() throws Exception {
        long testId = 1L;

        mockMvc.perform(get("/organizer/tests/{testId}/results", testId))
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/results_overview"))
                .andExpect(model().attributeExists("results"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann einen Test für einen Studenten als nicht bestanden markieren")
    void testOrganizerFailStudentTest() throws Exception {
        long testId = 1L;
        String studentGithubId = "github|12345";
        String reason = "Plausibility check failed";

        mockMvc.perform(post("/organizer/tests/{testId}/fail/{studentId}", testId, studentGithubId)
                .param("reason", reason)
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann Ergebnisse als CSV exportieren")
    void testOrganizerExportResultsCsv() throws Exception {
        long testId = 1L;
        String csvData = "UserId,Score,Passed\nstudent1,80.0,true\ngithub|12345,45.5,false";
        byte[] csvBytes = csvData.getBytes(StandardCharsets.UTF_8);

        when(csvExportService.generateResultsCsv(testId)).thenReturn(csvBytes);

        mockMvc.perform(get("/organizer/tests/{testId}/results/export", testId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"results_test_" + testId + ".csv\""))
                .andExpect(content().bytes(csvBytes));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann die Übersicht aller Freitextantworten für eine Frage sehen")
    void testOrganizerFreeTextAnswersOverview() throws Exception {
        long questionId = 5L;

        mockMvc.perform(get("/organizer/questions/{questionId}/submissions", questionId))
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/freetext_submissions"))
                .andExpect(model().attributeExists("submissions"));
    }
}