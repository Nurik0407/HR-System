package com.example.border.model.entity;

import com.example.border.model.enums.EducationLevel;
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
    private EducationLevel educationLevel;
    private String institution;
    private LocalDate graduationDate;

    public Education(EducationLevel educationLevel, String institution, LocalDate graduationDate) {
        this.educationLevel = educationLevel;
        this.institution = institution;
        this.graduationDate = graduationDate;
    }
}