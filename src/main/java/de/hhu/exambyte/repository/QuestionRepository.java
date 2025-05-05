package de.hhu.exambyte.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.hhu.exambyte.domain.model.Question;

/**
 * Spring Data JDBC Repository für Question Entitäten.
 * ACHTUNG: Question ist KEIN Aggregate Root, daher sind die Operationen hier
 * normalerweise nicht vorgesehen. Wir brauchen es hier aber zum Nachladen
 * von Fragendetails basierend auf der ID, die wir aus der Submission kennen.
 * Spring Data JDBC erlaubt das Laden via findById auch für Nicht-Roots.
 */
@Repository
public interface QuestionRepository extends CrudRepository<Question, Long> {
    // findById(Long id) wird von CrudRepository bereitgestellt.
}
