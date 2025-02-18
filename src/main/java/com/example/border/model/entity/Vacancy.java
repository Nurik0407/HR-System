package com.example.border.model.entity;

import com.example.border.model.enums.*;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
    private int applicationsCount;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;
    @Enumerated(EnumType.STRING)
    private Experience experience;
    private String additionalInfo;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Country country;
    private String city;
    private String contactInformation;

    @Formula("CASE " +
            "WHEN amount_type = 'FIXED' THEN fixed_amount " +
            "WHEN amount_type = 'RANGE' THEN max_amount " +
            "ELSE min_amount END")
    private Integer amount;

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private Employer employer;

    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VacancyApplication> vacancyApplications;

    public Vacancy(@Nullable Position position, boolean isOtherPositionSelected, @Nullable String otherPosition,
                   Industry industry, String vacancyDescription, String requiredSkills,
                   AmountType amountType, BigDecimal fixedAmount, BigDecimal maxAmount,
                   BigDecimal minAmount, Currency currency, EmploymentType employmentType,
                   Experience experience, Country country, String city,
                   String contactInformation, String additionalInfo) {
        this.position = position;
        this.isOtherPositionSelected = isOtherPositionSelected;
        this.otherPosition = otherPosition;
        this.industry = industry;
        this.vacancyDescription = vacancyDescription;
        this.requiredSkills = requiredSkills;
        this.amountType = amountType;
        this.fixedAmount = fixedAmount;
        this.maxAmount = maxAmount;
        this.minAmount = minAmount;
        this.currency = currency;
        this.employmentType = employmentType;
        this.experience = experience;
        this.country = country;
        this.city = city;
        this.contactInformation = contactInformation;
        this.additionalInfo = additionalInfo;
    }
}
