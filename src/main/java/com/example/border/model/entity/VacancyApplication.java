package com.example.border.model.entity;

import com.example.border.model.enums.ApplicationStatus;
import jakarta.persistence.*;

@Entity
public class VacancyApplication extends BaseEntity {

    private String coverLetter;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    @ManyToOne
    @JoinColumn(name = "vacancy_id")
    private Vacancy vacancy;

    @ManyToOne
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;
}
