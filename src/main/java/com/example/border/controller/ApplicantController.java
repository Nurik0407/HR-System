package com.example.border.controller;

import com.example.border.model.dto.applicant.ApplicantDto;
import com.example.border.model.dto.vacancy.VacanciesResponse;
import com.example.border.model.enums.*;
import com.example.border.service.ApplicantService;
import com.example.border.service.VacancyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applicant")
@Tag(name = "Соискатели", description = "API для работы с данными соискателей")
public class ApplicantController {

    private final ApplicantService applicantService;
    private final VacancyService vacancyService;

    public ApplicantController(ApplicantService applicantService, VacancyService vacancyService) {
        this.applicantService = applicantService;
        this.vacancyService = vacancyService;
    }

    @GetMapping
    @Operation(
            summary = "Получение данных соискателя",
            description = "Возвращает данные текущего соискателя."
    )
    public ResponseEntity<ApplicantDto> getProfile() {
        return ResponseEntity.ok(applicantService.findCurrentApplicant());
    }

    @PutMapping
    @Operation(
            summary = "Обновление данных соискателя",
            description = "Обновляет информацию текущего соискателя."
    )
    public ResponseEntity<ApplicantDto> updateApplication(
            @Valid @RequestBody ApplicantDto applicantDto) {
        return ResponseEntity.ok(applicantService.updateCurrentApplicant(applicantDto));
    }

    @GetMapping("/vacancies")
    @Operation(
            summary = "Получение списка вакансий",
            description = "Возвращает страницу вакансий с возможностью фильтрации и сортировки."
    )
    public ResponseEntity<Page<VacanciesResponse>> getVacancies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "createdAt,desc") String createdAtSort,
            @RequestParam(required = false) String amountSort,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Industry industry,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) Country country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Experience experience,
            @RequestParam(required = false) EmploymentType employmentType
    ) {
        return ResponseEntity.ok(vacancyService.getAllVacancies(
                searchQuery, industry, position,
                country, city, experience, employmentType, createdAtSort, amountSort, page, size));
    }
}
