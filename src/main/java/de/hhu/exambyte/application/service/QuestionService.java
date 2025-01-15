package de.hhu.exambyte.application.service;

import java.util.List;
import java.util.NoSuchElementException;

import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.infrastructure.persistence.repository.QuestionRepository;

public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Question getQuestionById(String id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Frage nicht gefunden für id: " + id));
    }

    public Question getFirstQuestion(Test test) {
        if (test != null && !test.getQuestions().isEmpty()) {
            return test.getQuestions().get(0); // Gibt die erste Frage der Liste zurück
        } else {
            return null; // Oder eine Ausnahme werfen, wenn keine Fragen vorhanden sind
        }
    }

    public Question getNextQuestion(Test test, Question currentQuestion) {
        List<Question> questions = test.getQuestions();

        // Überprüfen, ob der Test überhaupt Fragen hat
        if (questions == null || questions.isEmpty()) {
            return null; // Oder wirft eine Ausnahme, je nach Bedarf
        }

        // Finden der aktuellen Frage in der Liste
        int currentIndex = questions.indexOf(currentQuestion);

        // Überprüfen, ob die aktuelle Frage im Test gefunden wurde
        if (currentIndex == -1) {
            throw new IllegalArgumentException("Aktuelle Frage nicht im Test gefunden");
        }

        // Berechne den Index der nächsten Frage
        int nextIndex = currentIndex + 1;

        // Überprüfen, ob es eine nächste Frage gibt
        if (nextIndex < questions.size()) {
            return questions.get(nextIndex);
        } else {
            return null; // Oder könnte z.B. die erste Frage zurückgeben oder eine Ausnahme werfen
        }
    }

    public List<Question> getAllQuestions(Test test) {
        return test.getQuestions();
    }

}
