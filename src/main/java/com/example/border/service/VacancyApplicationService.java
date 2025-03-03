package com.example.border.service;

import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;

import java.util.UUID;

public interface VacancyApplicationService {
    String applyForVacancy(UUID vacancyId, VacancyApplicationRequest request);
}
