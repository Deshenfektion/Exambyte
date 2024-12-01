package de.hhu.exambyte.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/corrector")
public class CorrectorController {

    @GetMapping("")
    @Secured("ROLE_CORRECTOR")
    public String indexCorrector(OAuth2AuthenticationToken auth, Model m) {
        String login = auth.getPrincipal().getAttribute("login");
        System.out.println(auth);
        m.addAttribute("name", login);
        return "corrector-dashboard";
    }
}
