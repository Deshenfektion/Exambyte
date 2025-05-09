package de.hhu.exambyte.controller;

import de.hhu.exambyte.configuration.MethodSecurity;
import de.hhu.exambyte.helper.WithMockOAuth2User;
import de.hhu.exambyte.service.TestService;

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
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann das Dashboard sehen")
    void testStudentDashboardAccess() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/dashboard"))
                .andExpect(model().attributeExists("tests", "admissionStatus"));
    }

    @Test
    @WithMockUser(roles = { "ORGANIZER", "CORRECTOR" })
    @DisplayName("Nicht-Studenten können nicht auf das Studenten-Dashboard zugreifen")
    void testNonStudentDashboardAccess() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Anonyme Benutzer werden zum Login weitergeleitet")
    void testAnonymousDashboardAccess() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/github"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann die Testseite sehen (nach Start, vor Ende)")
    void testStudentTestViewAccess() throws Exception {
        long testId = 1L;

        mockMvc.perform(get("/student/tests/{testId}", testId))
                .andExpect(status().isOk())
                .andExpect(view().name("student/test_view"))
                .andExpect(model().attributeExists("test"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann die Ergebnisseite sehen (nach Veröffentlichung)")
    void testStudentResultsViewAccess() throws Exception {
        long testId = 1L;

        mockMvc.perform(get("/student/results/{testId}", testId))
                .andExpect(status().isOk())
                .andExpect(view().name("student/results_view"))
                .andExpect(model().attributeExists("result"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann Ergebnisseite nicht vor Veröffentlichung sehen")
    void testStudentResultsViewAccessBeforePublication() throws Exception {
        long testId = 1L;
        when(resultService.getResultsForStudentIfPublished(eq(testId), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/student/results/{testId}", testId))
                .andExpect(status().isForbidden());
    }
}