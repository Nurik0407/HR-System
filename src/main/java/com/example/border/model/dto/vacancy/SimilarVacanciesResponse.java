package com.example.border.model.dto.vacancy;

import com.example.border.model.enums.AmountType;
import com.example.border.model.enums.Country;
import com.example.border.model.enums.EmploymentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SimilarVacanciesResponse(
        UUID vacancyId,
        String companyName,
        String position,
        AmountType amountType,
        BigDecimal fixedAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        EmploymentType employmentType,
        Country country,
        String city
) {
}
