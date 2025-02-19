package com.example.border.controller;

import com.example.border.model.dto.employer.VacancyResponse;
import com.example.border.model.dto.vacancy.VacanciesResponse;
import com.example.border.model.enums.*;
import com.example.border.service.VacancyService;
import com.example.border.model.dto.vacancy.SimilarVacanciesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vacancies")
@Tag(name = "Вакансии", description = "API для работы с активными, публичными вакансиями")
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
        return ResponseEntity.ok(vacancyService.getActiveVacancies(
                searchQuery, industry, position,
                country, city, experience, employmentType, createdAtSort, amountSort, page, size));
    }

    @GetMapping("/{vacancyId}")
    @Operation(
            summary = "Получить вакансию по ID",
            description = "Получение детальной информации о вакансии по её уникальному ID."
    )
    public ResponseEntity<VacancyResponse> getVacancy(@PathVariable UUID vacancyId) {
        return ResponseEntity.ok(vacancyService.getVacancy(vacancyId));
    }

    @GetMapping("/{vacancyId}/similar")
    @Operation(
            summary = "Получение похожих вакансий",
            description = "Возвращает страницу вакансий, которые похожи на указанную вакансию по позициям, зарплатам и другим фильтрам."
    )
    public ResponseEntity<Page<SimilarVacanciesResponse>> getSimilarVacancies(
            @PathVariable UUID vacancyId,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(vacancyService.findSimilarVacancies(vacancyId,size,page));
    }
}
