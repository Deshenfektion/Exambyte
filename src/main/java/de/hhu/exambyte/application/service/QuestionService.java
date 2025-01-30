package de.hhu.exambyte.application.service;

import java.util.List;
import java.util.NoSuchElementException;

import de.hhu.exambyte.domain.model.Question;
import de.hhu.exambyte.domain.model.Test;
import de.hhu.exambyte.infrastructure.persistence.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Question getQuestionById(int id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Frage nicht gefunden für ID: " + id));
    }

    public Question getFirstQuestion(Test test) {
        return questionRepository.findFirstByTestIdOrderByIdAsc(test.getId())
                .orElseThrow(() -> new NoSuchElementException("Keine Fragen für Test ID: " + test.getId()));
    }

    public Question getNextQuestion(Test test, Question currentQuestion) {
        return questionRepository
                .findFirstByTestIdAndIdGreaterThanOrderByIdAsc(test.getId(), currentQuestion.getId())
                .orElse(null); // Gibt null zurück, wenn es keine nächste Frage gibt
    }

    public List<Question> getAllQuestions(Test test) {
        return questionRepository.findByTestIdOrderByIdAsc(test.getId());
    }
}