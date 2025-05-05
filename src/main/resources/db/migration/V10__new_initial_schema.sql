CREATE TABLE tests (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    publish_time TIMESTAMP NOT NULL
);

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL REFERENCES tests(id) ON DELETE CASCADE, -- FK zum Aggregat-Root
    question_text TEXT NOT NULL,
    max_points NUMERIC(5, 2) NOT NULL CHECK (max_points > 0), -- Beispiel: Max 999.99 Punkte
    type VARCHAR(10) NOT NULL, -- 'MC' oder 'FREETEXT'
    solution_proposal TEXT -- Kann NULL sein
);
CREATE INDEX idx_questions_test_id ON questions(test_id); -- Index für FK

CREATE TABLE answer_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE, -- FK zur Frage
    option_text TEXT NOT NULL,
    correct BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_answer_options_question_id ON answer_options(question_id); -- Index für FK

CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL, -- Referenz, FK optional aber empfohlen
    question_id BIGINT NOT NULL, -- Referenz, FK optional aber empfohlen
    student_github_id VARCHAR(255) NOT NULL, -- Eindeutige ID des Studenten
    submitted_text TEXT, -- Antwort bei Freitext
    -- Hier könnten später Spalten für MC-Antworten hinzukommen
    score NUMERIC(5, 2), -- Erreichte Punkte, NULL wenn unbewertet
    feedback TEXT, -- Feedback, NULL wenn keins
    -- Optional, aber empfohlen für Datenintegrität:
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE SET NULL, -- Oder CASCADE, je nach Anforderung
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE SET NULL -- Oder CASCADE
);
CREATE INDEX idx_submissions_test_question_student ON submissions(test_id, question_id, student_github_id);
CREATE INDEX idx_submissions_test_id ON submissions(test_id);
CREATE INDEX idx_submissions_question_id ON submissions(question_id);