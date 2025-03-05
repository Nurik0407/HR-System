package com.example.border.service;

import com.example.border.model.dto.employer.candidate.VacancyCandidatesResponse;
import com.example.border.model.dto.vacancyApplication.ApplicantApplicationsResponse;
import com.example.border.model.dto.vacancyApplication.InviteInterviewRequest;
import com.example.border.model.dto.vacancyApplication.PositionResponse;
import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;
import com.example.border.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface VacancyApplicationService {
    String applyForVacancy(UUID vacancyId, VacancyApplicationRequest request);

    Page<ApplicantApplicationsResponse> getMyApplications(
            int page, int size, String keyword, ApplicationStatus status, String dateFilter);

    Page<VacancyCandidatesResponse> getCandidates(
            int page, int size, String keyWord, String experience, ApplicationStatus status, String dateFilter);

    String updateStatus(UUID applicationId, ApplicationStatus newStatus, InviteInterviewRequest request);

    List<PositionResponse> getPositionByApplicant(UUID applicantId);
}
