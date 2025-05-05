package de.hhu.exambyte.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Repräsentiert einen Test (Klausur/Übung) als Aggregate Root.
 * Enthält Metadaten und die zugehörigen Fragen.
 */
@Table("tests") // Map record to the 'tests' table
public record Test(
        @Id Long id, // Primary Key
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime publishTime, // Ergebnis-Veröffentlichungszeitpunkt

        // One-to-Many-Beziehung: Ein Test hat mehrere Fragen.
        // Spring Data JDBC lädt diese mit, wenn der Test geladen wird.
        // 'test_id' ist die Spalte in der 'questions' Tabelle, die auf 'tests.id'
        // verweist.
        // 'keyColumn' ist die ID-Spalte in der 'questions' Tabelle selbst.
        @MappedCollection(idColumn = "test_id", keyColumn = "id") Set<Question> questions) {
    // Konstruktor für die Erstellung neuer Tests ohne ID (wird von DB generiert)
    public Test(String title, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime publishTime,
            Set<Question> questions) {
        this(null, title, startTime, endTime, publishTime, questions);
    }
}
