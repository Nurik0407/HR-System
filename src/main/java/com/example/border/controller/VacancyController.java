package com.example.border.controller;

import com.example.border.model.dto.vacancy.VacanciesResponse;
import com.example.border.model.enums.*;
import com.example.border.service.VacancyService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @GetMapping
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
