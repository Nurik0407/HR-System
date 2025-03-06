package com.example.border.service;

import com.example.border.model.dto.applicant.ApplicantDto;
import com.example.border.model.entity.Applicant;

import java.util.UUID;

public interface ApplicantService {
    ApplicantDto getApplicantById(UUID applicantId);

    ApplicantDto updateCurrentApplicant(ApplicantDto applicantDto);

    Applicant findApplicantById(UUID applicantId);
}
