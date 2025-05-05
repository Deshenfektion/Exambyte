-- V2__Add_MC_Submissions.sql

-- Tabelle, um die vom Studenten ausgewählten Antwortoptionen für eine Submission zu speichern
CREATE TABLE submission_selected_options (
    submission_id BIGINT NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    answer_option_id BIGINT NOT NULL REFERENCES answer_options(id) ON DELETE CASCADE,
    PRIMARY KEY (submission_id, answer_option_id) -- Stellt sicher, dass eine Option pro Submission nur einmal gewählt wird
);

-- Index zur Beschleunigung von Abfragen basierend auf der Submission ID
CREATE INDEX idx_submission_selected_options_submission_id ON submission_selected_options(submission_id);

-- Optional: Index für die andere Richtung (falls benötigt)
-- CREATE INDEX idx_submission_selected_options_option_id ON submission_selected_options(answer_option_id);