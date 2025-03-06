package com.example.border.service;

import com.example.border.model.dto.applicant.ApplicantDto;
import com.example.border.model.dto.applicant.ApplicantsResponse;
import com.example.border.model.entity.Applicant;
import com.example.border.model.enums.Country;
import com.example.border.model.enums.EducationLevel;
import com.example.border.model.enums.Position;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ApplicantService {
    ApplicantDto getApplicantById(UUID applicantId);

    ApplicantDto updateCurrentApplicant(ApplicantDto applicantDto);

    Applicant findApplicantById(UUID applicantId);

    Page<ApplicantsResponse> getApplicants(int page,
                                           int size,
                                           String keyWord,
                                           Position position,
                                           EducationLevel educationLevel,
                                           Country country,
                                           String city,
                                           String experience);
}
