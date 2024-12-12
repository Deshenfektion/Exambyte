package de.hhu.exambyte.application.service;

import de.hhu.exambyte.domain.model.Test;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestService {

    private List<Test> loadTestsFromDatabaseForStudents() {
        // TODO: Implementiere Datenbankabfrage
        // Vorübergehend: Rückgabe einer leeren Liste oder Mockdaten
        return new ArrayList<>();
    }

    private List<Test> loadTestsFromDatabaseForOrganizers() {
        // TODO: Implementiere Datenbankabfrage
        // Vorübergehend: Rückgabe einer leeren Liste oder Mockdaten
        return new ArrayList<>();
    }

    private List<Test> loadTestsFromDatabaseForCorrectors() {
        // TODO: Implementiere Datenbankabfrage
        // Vorübergehend: Rückgabe einer leeren Liste oder Mockdaten
        return new ArrayList<>();
    }

    public List<Test> getTestsForStudents() {
        // Tests: veröffentlicht + vor Endzeitpunkt
        return loadTestsFromDatabaseForStudents();
    }

    public List<Test> getTestsForOrganizers() {
        // Alle Tests
        return loadTestsFromDatabaseForOrganizers();
    }

    public List<Test> getTestsForCorrectors() {
        // Tests: Unkorrigierte Freitextaufgaben
        return loadTestsFromDatabaseForCorrectors();
    }

    public Test getTestById(String id) {
        // Datenbankabfrage
        // return Test
    }

}