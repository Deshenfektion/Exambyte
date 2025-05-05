package de.hhu.exambyte.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.hhu.exambyte.domain.model.Test;

/**
 * Spring Data JDBC Repository für das Test-Aggregat.
 * Verwaltet Test-Entitäten inklusive der zugehörigen Questions und
 * AnswerOptions.
 */
@Repository // Markiert dies als Spring Bean und Repository
public interface TestRepository extends CrudRepository<Test, Long> {

    // Standard-CRUD-Methoden (save, findById, findAll, deleteById, etc.)
    // werden von CrudRepository bereitgestellt.

    // Hier könnten später benutzerdefinierte Abfragen hinzugefügt werden,
    // z.B. finde alle Tests, deren Startzeit in der Zukunft liegt:
    // List<Test> findByStartTimeAfter(LocalDateTime time);

    // Oder finde alle Tests (evtl. nur Titel und ID für eine Übersicht)
    // @Query("SELECT id, title FROM tests ORDER BY start_time DESC")
    // List<TestSummary> findAllSummaries();
    // (Wobei TestSummary ein separates DTO/Record wäre)
}