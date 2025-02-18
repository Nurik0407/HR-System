package com.example.border.model.dto.employer;

import com.example.border.model.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VacancyResponse(
        UUID vacancyId,
        LocalDate creationDate,
        Position position,
        boolean isOtherPositionSelected,
        String otherPosition,
        int applicationsCount,
        String companyName,
        String logoUrl,
        String aboutCompany,
        String vacancyDescription,
        String requiredSkills,
        String contactInformation,
        String additionalInfo,
        Status status,
        String location,
        Industry industry,
        EmploymentType employmentType,
        Experience experience,
        AmountType amountType,
        BigDecimal fixedAmount,
        BigDecimal maxAmount,
        BigDecimal minAmount,
        Currency currency
) {
}
