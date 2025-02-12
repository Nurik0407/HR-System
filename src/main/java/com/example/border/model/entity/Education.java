package com.example.border.model.entity;

import com.example.border.model.enums.EducationDegree;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Education extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private EducationDegree educationDegree;
    private String institution;
    private LocalDate graduationDate;
}