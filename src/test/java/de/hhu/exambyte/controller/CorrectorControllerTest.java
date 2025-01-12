package de.hhu.exambyte.controller;

import de.hhu.exambyte.application.service.TestService;
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

@WebMvcTest(CorrectorController.class)
@Import({MethodSecurity.class})
public class CorrectorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private TestService testService;

    @Test
    @WithMockOAuth2User(roles = "CORRECTOR")
    @DisplayName("Corrector kann sich anmelden und Dashboard ist erreichbar")
    void testCorrectorAccess() throws Exception {
        mockMvc.perform(get("/corrector"))
                .andExpect(status().isOk())
                .andExpect(view().name("corrector/dashboard"));
    }

    @Test
    @WithMockOAuth2User(roles = "USER")
    @DisplayName("Normale Benutzer können nicht auf das Corrector-Dashboard zugreifen")
    void testUnauthorizedUserAccess() throws Exception {
        mockMvc.perform(get("/corrector"))
                .andExpect(status().isForbidden());
    }


}
