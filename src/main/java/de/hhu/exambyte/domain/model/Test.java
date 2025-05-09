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
@Table("tests")
public record Test(
        @Id Long id,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime publishTime,

        @MappedCollection(idColumn = "test_id", keyColumn = "id") Set<Question> questions) {
    public Test(String title, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime publishTime,
            Set<Question> questions) {
        this(null, title, startTime, endTime, publishTime, questions);
    }
}
