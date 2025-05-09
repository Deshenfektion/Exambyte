package de.hhu.exambyte.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import de.hhu.exambyte.configuration.MethodSecurity;
import de.hhu.exambyte.service.TestService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Zielcontroller anpassen
@WebMvcTest(TestExecutionController.class)
@Import(MethodSecurity.class)
public class TestExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestService testService;

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann während des Testzeitraums auf eine Frage zugreifen")
    void testStudentAccessQuestionDuringTest() throws Exception {
        long testId = 1L;
        long questionId = 10L;

        mockMvc.perform(get("/student/tests/{testId}/questions/{questionId}", testId, questionId))
                .andExpect(status().isOk())
                .andExpect(view().name("student/question_view"))
                .andExpect(model().attributeExists("question", "submission"));

    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann während des Testzeitraums eine Antwort abschicken")
    void testStudentSubmitAnswerDuringTest() throws Exception {
        long testId = 1L;
        long questionId = 10L;
        mockMvc.perform(post("/student/tests/{testId}/submit", testId)

                .param("answers[" + questionId + "].freetext", "Antwort des Studenten")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Student kann nach Testende keine Antwort mehr abschicken")
    void testStudentSubmitAnswerAfterTestEnd() throws Exception {
        long testId = 1L;
        long questionId = 10L;

        mockMvc.perform(post("/student/tests/{testId}/submit", testId)
                .param("answers[" + questionId + "].freetext", "Verspätete Antwort")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    @DisplayName("Organisator kann nicht auf die Testdurchführungsseite zugreifen")
    void testOrganizerCannotTakeTest() throws Exception {
        long testId = 1L;
        long questionId = 10L;
        mockMvc.perform(get("/student/tests/{testId}/questions/{questionId}", testId, questionId))
                .andExpect(status().isForbidden());

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

        mockMvc.perform(get("/student/tests/{testId}/questions/{questionId}", testId, questionId))
                .andExpect(status().isOk())
                .andExpect(view().name("student/question_view"))
                .andExpect(model().attribute("isReadOnly", true))
                .andExpect(model().attributeExists("question", "submission"));

    }
}