package de.hhu.exambyte.controller;

import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.application.service.QuestionService;
import de.hhu.exambyte.application.service.TestService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private TestService testService;
    private QuestionService questionService;

    @GetMapping("")
    @Secured("ROLE_STUDENT")
    public String indexStudent(OAuth2AuthenticationToken auth, Model m) {
        String login = auth.getPrincipal().getAttribute("login");
        System.out.println(auth);
        m.addAttribute("name", login);

        List<Test> allTests = testService.getTestsForStudents();
        m.addAttribute("tests", allTests);

        return "student-dashboard";
    }

    @GetMapping("/test/{testId}/overview")
    @Secured("ROLE_STUDENT")
    public String testOverview(@PathVariable String testId, Model model) {
        Test test = testService.getTestById(testId);
        model.addAttribute("test", test);

        /*
         * Question dem Spring Model hinzufügen, damit später direkt die erste Frage in
         * der Test Session angezeigt werden kann
         */
        Question question = questionService.getFirstQuestion(test);
        model.addAttribute("firstQuestion", question);
        return "student-test-overview";
    }

    /*
     * PathVariable statt RequestParam, weil ids unerlässlich sind
     */
    @GetMapping("test/{testId}/question/{questionId}")
    public String testSession(@PathVariable String id, Model model) {
        Test test = testService.getTestById(id);
        model.addAttribute("test", test);

        test.setStatus("IN_PROGRESS");
        testService.setStatus(test, "IN_PROGRESS");

        Question currentQuestion = questionService.getNextQuestion(test);
        model.addAttribute("currentQuestion", currentQuestion);

        List<Question> allQuestions = questionService.getAllQuestions(test);
        model.addAttribute("allQuestions", allQuestions);

        return "student-test-session";
    }

}
