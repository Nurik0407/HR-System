package com.example.border.model.entity;

import com.example.border.model.enums.*;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Vacancy extends BaseEntity {

    @Nullable
    @Enumerated(EnumType.STRING)
    private Position position;
    @Nullable
    private String otherPosition;
    private boolean isOtherPositionSelected;
    @Enumerated(EnumType.STRING)
    private Industry industry;
    private String vacancyDescription;
    private String requiredSkills;

    @Enumerated(EnumType.STRING)
    private AmountType amountType;
    private BigDecimal fixedAmount;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;
    @Enumerated(EnumType.STRING)
    private Experience experience;
    private String additionalInfo;
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.REMOVE, orphanRemoval = true)
    List<VacancyApplication> vacancyApplications;
}
