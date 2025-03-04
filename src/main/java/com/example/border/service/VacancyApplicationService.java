package com.example.border.service;

import com.example.border.model.dto.vacancyApplication.ApplicantApplicationsResponse;
import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;
import com.example.border.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface VacancyApplicationService {
    String applyForVacancy(UUID vacancyId, VacancyApplicationRequest request);

    Page<ApplicantApplicationsResponse> getMyApplications(
            int page, int size, String keyword, ApplicationStatus status, String dateFilter);

}
