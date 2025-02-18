package com.example.border.service;

import com.example.border.model.dto.employer.VacanciesResponseForEmployer;
import com.example.border.model.dto.employer.VacancyDto;
import com.example.border.model.dto.employer.VacancyResponse;
import com.example.border.model.enums.Status;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface VacancyService {
    Page<VacanciesResponseForEmployer> getVacancies(
            int page, int size, String sort,
            String searchQuery, Status status, String createdDateRange);

    VacancyResponse getVacancy(UUID vacancyId);

    String createVacancy(VacancyDto request);

    VacancyDto updateVacancy(UUID vacancyId, VacancyDto vacancyDto);

    String deleteById(UUID vacancyId);

    String changeStatus(UUID vacancyId, Status status);
}
