package com.example.border.model.entity;

import com.example.border.model.enums.EducationDegree;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Education extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private EducationDegree educationDegree;
    private String institution;
    private LocalDate graduationDate;

    public Education(EducationDegree educationDegree, String institution, LocalDate graduationDate) {
        this.educationDegree = educationDegree;
        this.institution = institution;
        this.graduationDate = graduationDate;
    }
}