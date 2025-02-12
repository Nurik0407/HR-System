package com.example.border.model.entity;

import com.example.border.model.enums.ApplicationStatus;
import jakarta.persistence.*;

@Entity
public class VacancyApplication extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    @ManyToOne
    @JoinColumn(name = "vacancy_id")
    Vacancy vacancy;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
