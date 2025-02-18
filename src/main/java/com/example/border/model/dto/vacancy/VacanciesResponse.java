package com.example.border.model.dto.vacancy;

import com.example.border.model.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VacanciesResponse(
        String companyName,
        String logoUrl,

        Country country,
        String city,
        String position,
        Industry industry,
        AmountType amountType,
        BigDecimal fixedAmount,
        BigDecimal maxAmount,
        BigDecimal minAmount,
        Currency currency,
        EmploymentType employmentType,
        Experience experience
) {
    public VacanciesResponse(
            String companyName,
            String logoUrl,
            Country country,
            String city,
            String position,
            Industry industry,
            AmountType amountType,
            BigDecimal fixedAmount,
            BigDecimal maxAmount,
            BigDecimal minAmount,
            Currency currency,
            EmploymentType employmentType,
            Experience experience) {
        this.companyName = companyName;
        this.logoUrl = logoUrl;
        this.country = country;
        this.city = city;
        this.position = position;
        this.industry = industry;
        this.amountType = amountType;
        this.fixedAmount = (amountType == AmountType.FIXED) ? fixedAmount : null;
        this.maxAmount = (amountType == AmountType.RANGE) ? maxAmount : null;
        this.minAmount = (amountType != AmountType.FIXED) ? minAmount : null;
        this.currency = currency;
        this.employmentType = employmentType;
        this.experience = experience;
    }
}
