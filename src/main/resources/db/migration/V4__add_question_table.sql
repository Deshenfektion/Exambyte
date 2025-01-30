CREATE TABLE question (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    question_type VARCHAR(20) NOT NULL CHECK (question_type IN ('MULTIPLE_CHOICE', 'TEXT_BASED')),
    correction_status BOOLEAN DEFAULT FALSE,
    uncorrected_textbased_question BOOLEAN DEFAULT FALSE
);