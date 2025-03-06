package com.example.border.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Favorite extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private Employer employer;

    @ManyToOne
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;

    public Favorite(Employer employer, Applicant applicant) {
        this.employer = employer;
        this.applicant = applicant;
    }
}
