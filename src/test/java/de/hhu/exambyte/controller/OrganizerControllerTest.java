package de.hhu.exambyte.controller;

import de.hhu.exambyte.application.service.TestService;
import de.hhu.exambyte.configuration.MethodSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import de.hhu.exambyte.helper.WithMockOAuth2User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OrganizerController.class)
@Import({MethodSecurity.class})
public class OrganizerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private TestService testService;

    @Test
    @WithMockOAuth2User(roles = "ORGANIZER")
    @DisplayName("Organizer kann sich anmelden und Dashboard ist erreichbar")
    void testOrganizerAccess() throws Exception {
        mockMvc.perform(get("/organizer"))
                .andExpect(status().isOk())
                .andExpect(view().name("organizer/dashboard"));
    }

    @Test
    @WithMockOAuth2User(roles = "USER")
    @DisplayName("Normale Benutzer können nicht auf das Organizer-Dashboard zugreifen")
    void testUnauthorizedUserAccess() throws Exception {
        mockMvc.perform(get("/organizer"))
                .andExpect(status().isForbidden());
    }
}
