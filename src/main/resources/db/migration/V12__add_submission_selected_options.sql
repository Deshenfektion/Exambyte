-- V12__Add_index_on_answer_option_id.sql
-- Beschreibung: Fügt den fehlenden Index auf answer_option_id zur Tabelle submission_selected_options hinzu,
--              die bereits in einer früheren Migration (V11 bzw. V2) erstellt wurde.

-- Index für Abfragen basierend auf der Answer Option ID hinzufügen.
-- Der Index auf submission_id wurde bereits in V11 (als idx_submission_selected_options_submission_id) erstellt.
CREATE INDEX IF NOT EXISTS idx_sso_answer_option_id ON submission_selected_options(answer_option_id);

-- Optional: Wenn du den Indexnamen für submission_id vereinheitlichen willst:
-- 1. Alten Index löschen (vorsichtig!):
-- DROP INDEX IF EXISTS idx_submission_selected_options_submission_id;
-- 2. Neuen Index erstellen:
-- CREATE INDEX IF NOT EXISTS idx_sso_submission_id ON submission_selected_options(submission_id);
-- Oder umbenennen (wenn deine PostgreSQL-Version es unterstützt und der Index sicher existiert):
-- ALTER INDEX idx_submission_selected_options_submission_id RENAME TO idx_sso_submission_id;
-- Für den Moment ist es aber am sichersten, nur den fehlenden Index hinzuzufügen.