package de.hhu.exambyte.controller;

import de.hhu.exambyte.configuration.MethodSecurity;
import de.hhu.exambyte.service.CorrectionService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CorrectorController.class)
@Import(MethodSecurity.class)
public class CorrectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CorrectionService correctionService;

    @Test
    @WithMockUser(roles = "CORRECTOR")
    @DisplayName("Korrektor kann das Korrektur-Dashboard sehen")
    void testCorrectorDashboardAccess() throws Exception {
        mockMvc.perform(get("/corrector/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("corrector/dashboard"))
                .andExpect(model().attributeExists("submissionsToCorrect"));
    }

    @Test
    @WithMockUser(roles = { "STUDENT", "ORGANIZER" })
    @DisplayName("Nicht-Korrektoren können nicht auf das Korrektur-Dashboard zugreifen")
    void testNonCorrectorDashboardAccess() throws Exception {
        mockMvc.perform(get("/corrector/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
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

        mockMvc.perform(get("/corrector/submissions/{submissionId}", submissionId))
                .andExpect(status().isOk())
                .andExpect(view().name("corrector/grade_submission"))
                .andExpect(model().attributeExists("submission"));
    }

    @Test
    @WithMockUser(roles = "CORRECTOR")
    @DisplayName("Korrektor kann eine Bewertung für eine Freitextaufgabe abschicken")
    void testCorrectorSubmitGrade() throws Exception {
        long submissionId = 1L;
        double score = 8.0;
        String feedback = "Gut gemacht!";

        mockMvc.perform(post("/corrector/submissions/{submissionId}", submissionId)
                .param("score", String.valueOf(score))
                .param("feedback", feedback)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/corrector/dashboard"));
    }

    @Test
    @WithMockUser(roles = "CORRECTOR")
    @DisplayName("Korrektor muss Feedback geben, wenn nicht maximale Punktzahl vergeben wird")
    void testCorrectorSubmitGradeRequiresFeedback() throws Exception {
        long submissionId = 1L;
        double score = 5.0;
        String feedback = "";

        mockMvc.perform(post("/corrector/submissions/{submissionId}", submissionId)
                .param("score", String.valueOf(score))
                .param("feedback", feedback)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("corrector/grade_submission"))
                .andExpect(model().attributeHasFieldErrors("gradeCommand", "feedback"));
    }
}