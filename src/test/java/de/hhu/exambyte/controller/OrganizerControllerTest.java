package de.hhu.exambyte.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.hhu.exambyte.controller.OrganizerController;
import de.hhu.exambyte.helper.WithMockOAuth2User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizerController.class)
public class OrganizerControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockOAuth2User(roles = "ORGANIZER")
    @DisplayName("Organizer Dashboard ist erreichbar unter /organizer")
    void test1() throws Exception {
        mockMvc.perform(get("/organizer")).andExpect(status().isOk());
    }
}
