package com.example.border.model.dto.employer;

import com.example.border.model.enums.AmountType;
import com.example.border.model.enums.EmploymentType;
import com.example.border.model.enums.Experience;
import com.example.border.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VacanciesResponseForEmployer(
        UUID vacancyId,
        String position,
        EmploymentType employmentType,
        AmountType amountType,
        BigDecimal fixedAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Experience workExperience,
        int applicationsCount,
        LocalDateTime createdAt,
        Status status
) {
}
