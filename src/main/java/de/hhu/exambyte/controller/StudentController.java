package de.hhu.exambyte.controller;

import de.hhu.exambyte.model.Test;
import de.hhu.exambyte.service.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private TestService testService;

    @GetMapping("")
    public String showStudentDashboard(Model model) {
        List<Test> availableTests = testService.getAvailableTests();
        model.addAttribute("tests", availableTests);
        return "student-dashboard";
    }

/*    public String showNewTest() {

    }*/
}
