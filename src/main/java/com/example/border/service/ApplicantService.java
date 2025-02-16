package com.example.border.service;

import com.example.border.model.dto.applicant.ApplicantDto;

public interface ApplicantService {
    ApplicantDto findCurrentApplicant();

    ApplicantDto updateCurrentApplicant(ApplicantDto applicantDto);
}
