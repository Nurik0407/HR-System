package com.example.border.model.entity;

import com.example.border.model.enums.Position;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WorkExperience extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private Position position;
    private String companyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean currentJob;
    private String skills;

    public WorkExperience(Position position, String companyName, LocalDate startDate, LocalDate endDate, String skills, boolean currentJob) {
        this.position = position;
        this.companyName = companyName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.skills = skills;
        this.currentJob = currentJob;
    }
}
