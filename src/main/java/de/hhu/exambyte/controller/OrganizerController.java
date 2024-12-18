package de.hhu.exambyte.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.hhu.exambyte.domain.model.Test;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    @Autowired
    private de.hhu.exambyte.application.service.TestService testService;

    @GetMapping("")
    @Secured("ROLE_ORGANIZER")
    public String indexOrganizer(OAuth2AuthenticationToken auth, Model m) {
        String login = auth.getPrincipal().getAttribute("login");
        System.out.println(auth);
        m.addAttribute("name", login);

        List<Test> allTests = testService.getTestsForOrganizers();
        m.addAttribute("tests", allTests);

        return "organizer/dashboard";
    }

    @GetMapping("/new-test")
    @Secured("ROLE_ORGANIZER")
    public String newTest() {
        return "organizer/new-test";
    }
//
//    @PostMapping("/new-test")
//    @Secured("ROLE_ORGANIZER")
//    public String createNewTest(@RequestParam String choice) {
//        if ("ja".equalsIgnoreCase(choice)) {
//            return "redirect:/organizer/create-test";
//        } else {
//            return "redirect:/organizer";
//        }
//    }
//
//    @GetMapping("/test/{id}")
//    @Secured("ROLE_ORGANIZER")
//    public String viewTest(@PathVariable String id, Model model) {
//        Test test = testService.getTestById(id);
//        model.addAttribute("test", test);
//        return "test-details";
//    }

}
