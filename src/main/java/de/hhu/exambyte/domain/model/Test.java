package de.hhu.exambyte.domain.model;

import java.time.LocalDate;
import java.util.List;

public record Test(
        int id,
        String name,
        List<Question> questions,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate resultDate) {
}
