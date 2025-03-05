package com.example.border.service;

import com.example.border.model.dto.applicant.ApplicantDto;

import java.util.UUID;

public interface ApplicantService {
    ApplicantDto getApplicantById(UUID applicantId);

    ApplicantDto updateCurrentApplicant(ApplicantDto applicantDto);
}
