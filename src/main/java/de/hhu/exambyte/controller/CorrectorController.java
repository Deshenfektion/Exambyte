package de.hhu.exambyte.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.hhu.exambyte.interface.Question;
import de.hhu.exambyte.model.Test;
import de.hhu.exambyte.service.TestService;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/corrector")
public class CorrectorController {

    @Autowired
    private TestService testService;

    @GetMapping("")
    @Secured("ROLE_CORRECTOR")
    public String indexCorrector(OAuth2AuthenticationToken auth, Model m) {
        String login = auth.getPrincipal().getAttribute("login");
        System.out.println(auth);
        m.addAttribute("name", login);

        // Alle Tests, die unkorrigierte Freitextaufgaben enthalten
        List<Test> allTests = testService.getTestsForCorrector();

        return "corrector-dashboard";
    }

    @GetMapping("/test/{id}")
    @Secured("ROLE_CORRECTOR")
    public String viewTest(@PathVariable Long id, Model model) {
        Test test = testService.getTestById(id);
        model.addAttribute("test", test);

        // Finde die erste Frage für den Korrektor
        Question firstCorrectorQuestion = test.getQuestions().stream()
                .filter(Question::forCorrector)
                .findFirst()
                .orElse(null);

        model.addAttribute("currentQuestion", firstCorrectorQuestion);

        return "corrector-test-view";
    }

    @GetMapping("test/{testId}/question/{quetionId}")
    public String viewQuestion(@PathVariable Long testId, @PathVariable Long questionId, Model model) {
        Test test = testService.getTestById(testId);
        Question question = questionService.getQuestionById(questionId);

        model.addAttribute("test", test);
        model.addAttribute("currentQuestion", question);

        return "corrector-test-view";
    }
    
}
