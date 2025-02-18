package com.example.border.model.dto.employer;

import com.example.border.model.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VacancyDto(
        Position position,
        boolean isOtherPositionSelected,
        String otherPosition,

        @NotNull(message = "Industry must not be null")
        Industry industry,
        String vacancyDescription,
        String requiredSkills,
        @NotNull(message = "Amount type must not be null")
        AmountType amountType,
        @Positive(message = "Fixed amount should be more than 0")
        BigDecimal fixedAmount,
        @Positive(message = "Max amount should be more than 0")
        BigDecimal maxAmount,
        @Positive(message = "Min amount should be more than 0")
        BigDecimal minAmount,
        @NotNull(message = "Currency must not be null")
        Currency currency,
        @NotNull(message = "Employment type must not be null")
        EmploymentType employmentType,
        @NotNull(message = "Experience must not be null")
        Experience experience,
        Country country,
        String city,
        String contactInformation,
        String additionalInfo
) {
    public VacancyDto(
            Position position,
            boolean isOtherPositionSelected,
            String otherPosition,
            Industry industry,
            String vacancyDescription,
            String requiredSkills,
            AmountType amountType,
            BigDecimal fixedAmount,
            BigDecimal maxAmount,
            BigDecimal minAmount,
            Currency currency,
            EmploymentType employmentType,
            Experience experience,
            Country country,
            String city,
            String contactInformation,
            String additionalInfo) {
        this.position = (isOtherPositionSelected ? null : position);
        this.isOtherPositionSelected = isOtherPositionSelected;
        this.otherPosition = (isOtherPositionSelected) ? otherPosition : null;
        this.industry = industry;
        this.vacancyDescription = vacancyDescription;
        this.requiredSkills = requiredSkills;
        this.amountType = amountType;
        this.fixedAmount = (amountType == AmountType.FIXED) ? fixedAmount : null;
        this.maxAmount = (amountType == AmountType.FIXED || amountType == AmountType.FROM) ? null : maxAmount;
        this.minAmount = (amountType == AmountType.FIXED) ? null : minAmount;
        this.currency = currency;
        this.employmentType = employmentType;
        this.experience = experience;
        this.country = country;
        this.city = city;
        this.contactInformation = contactInformation;
        this.additionalInfo = additionalInfo;
    }
}
