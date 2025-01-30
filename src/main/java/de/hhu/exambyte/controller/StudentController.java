package de.hhu.exambyte.controller;

import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.domain.model.Test.TestStatus;
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

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private TestService testService;
    private QuestionService questionService;

    @GetMapping("")
    @Secured("ROLE_STUDENT")
    public String indexStudent(OAuth2AuthenticationToken auth, Model model) {
        String login = auth.getPrincipal().getAttribute("login");
        System.out.println(auth);
        model.addAttribute("name", login);

        List<Test> allTests = testService.getTestsForStudents();
        System.out.println("Alle Tests für Student Dashboard: " + allTests);
        model.addAttribute("tests", allTests);

        return "student/student-dashboard";
    }

    @GetMapping("/test/{testId}/overview")
    @Secured("ROLE_STUDENT")
    public String testOverview(@PathVariable int testId, Model model) {
        Test test = testService.getTestById(testId);
        model.addAttribute("test", test);

        /*
         * Question dem Spring Model hinzufügen, damit später direkt die erste Frage in
         * der Test Session angezeigt werden kann
         */
        Question question = questionService.getFirstQuestion(test);
        model.addAttribute("firstQuestion", question);
        return "student/student-test-overview";
    }

    /*
     * PathVariable statt RequestParam, weil ids unerlässlich sind
     */
    @GetMapping("test/{testId}/question/{questionId}")
    public String testSession(@PathVariable int testId, @PathVariable int questionId, Model model) {
        Test test = testService.getTestById(testId);
        model.addAttribute("test", test);

        test.setStatus(TestStatus.IN_PROGRESS);

        // Die aktuelle Frage anhand der ID abrufen
        Question currentQuestion = questionService.getQuestionById(questionId);
        model.addAttribute("currentQuestion", currentQuestion);

        // Die nächste Frage anzeigen
        currentQuestion = questionService.getNextQuestion(test, currentQuestion);
        model.addAttribute("currentQuestion", currentQuestion);

        // Für die Fragenübersicht in der Test Session View
        List<Question> allQuestions = questionService.getAllQuestions(test);
        model.addAttribute("allQuestions", allQuestions);

        return "student/student-test-session";
    }

}
