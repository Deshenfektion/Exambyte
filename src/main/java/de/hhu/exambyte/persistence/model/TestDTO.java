package de.hhu.exambyte.persistence.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "test")
public record TestDTO(
        @Id int id,
        String name,
        List<QuestionDTO> questions,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate resultDate) {

}
