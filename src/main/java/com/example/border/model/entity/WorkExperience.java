package com.example.border.model.entity;

import com.example.border.model.enums.Position;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class WorkExperience extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private Position position;
    private String companyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean currentJob;
    private String skills;
}
