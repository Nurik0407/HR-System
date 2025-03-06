package com.example.border.service;

import com.example.border.model.dto.employer.EmployerDto;

public interface EmployerService {
    EmployerDto getProfile();

    EmployerDto updateProfile(EmployerDto employerDto);
}
