package de.hhu.exambyte.controller;

import de.hhu.exambyte.model.Test;
import de.hhu.exambyte.service.TestService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private TestService testService;

    @GetMapping("")
    @Secured("ROLE_STUDENT")
    public String indexStudent(OAuth2AuthenticationToken auth, Model m) {
        String login = auth.getPrincipal().getAttribute("login");
        System.out.println(auth);
        m.addAttribute("name", login);

        // Alle Tests, die veröffenlicht worden und noch bearbeitbar sind
        List<Test> allTests = testService.getTestsForStudents();
        m.addAttribute("tests", allTests);

        return "student-dashboard";
    }

    @GetMapping("/test/{id}")
    @Secured("ROLE_STUDENT")
    public String viewTest(@PathVariable Long id, Model model) {
        Test test = testService.getTestById(id);
        model.addAttribute("test", test);
        return "student-test-view";
    }
}
