package com.example.border.service;

import com.example.border.model.dto.employer.VacanciesResponseForEmployer;
import com.example.border.model.dto.employer.VacancyDto;
import com.example.border.model.dto.employer.VacancyResponse;
import com.example.border.model.dto.vacancy.VacanciesResponse;
import com.example.border.model.enums.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface VacancyService {
    Page<VacanciesResponseForEmployer> getVacanciesTheEmployer(
            int page, int size, String sort,
            String searchQuery, Status status, String createdDateRange);

    VacancyResponse getVacancy(UUID vacancyId);

    String createVacancy(VacancyDto request);

    VacancyDto updateVacancy(UUID vacancyId, VacancyDto vacancyDto);

    String deleteById(UUID vacancyId);

    String changeStatus(UUID vacancyId, Status status);

    Page<VacanciesResponse> getAllVacancies(String searchQuery, Industry industry,
                                            Position position, Country country,
                                            String city, Experience experience,
                                            EmploymentType employmentType,
                                            String createdAtSort, String amountSort,
                                            int page, int size);
}
