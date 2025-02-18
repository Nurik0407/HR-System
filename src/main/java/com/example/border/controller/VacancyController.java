package com.example.border.controller;

import com.example.border.model.dto.employer.VacanciesResponseForEmployer;
import com.example.border.model.dto.employer.VacancyDto;
import com.example.border.model.dto.employer.VacancyResponse;
import com.example.border.model.enums.Status;
import com.example.border.service.VacancyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vacancies")
@Tag(name = "Вакансии работодателя")
public class VacancyController {

    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @GetMapping
    @Operation(
            summary = "Получить список вакансий",
            description = "Получение списка всех вакансий с возможностью фильтрации, сортировки и пагинации."
    )
    public ResponseEntity<Page<VacanciesResponseForEmployer>> getVacancies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String createdDateRange
    ) {
        return ResponseEntity.ok(vacancyService.getVacancies(page, size, sort, searchQuery, status, createdDateRange));
    }

    @GetMapping("/{vacancyId}")
    @Operation(
            summary = "Получить вакансию по ID",
            description = "Получение детальной информации о вакансии по её уникальному ID."
    )
    public ResponseEntity<VacancyResponse> getVacancy(@PathVariable UUID vacancyId) {
        return ResponseEntity.ok(vacancyService.getVacancy(vacancyId));
    }

    @PostMapping
    @Operation(
            summary = "Добавить новую вакансию",
            description = "Создание новой вакансии на платформе с использованием предоставленных данных."
    )
    public ResponseEntity<String> addVacancy(@Valid VacancyDto request) {
        return ResponseEntity.ok(vacancyService.createVacancy(request));
    }

    @PutMapping("/{vacancyId}")
    @Operation(
            summary = "Обновить вакансию",
            description = "Обновление информации о вакансии по её ID. Все переданные данные обновляют существующую вакансию."
    )
    public ResponseEntity<VacancyDto> updateVacancy(@PathVariable UUID vacancyId,
                                                    @Valid @RequestBody VacancyDto vacancyDto) {
        return ResponseEntity.ok(vacancyService.updateVacancy(vacancyId,vacancyDto));
    }

    @DeleteMapping("/{vacancyId}")
    @Operation(
            summary = "Удалить вакансию",
            description = "Удаление вакансии по её ID из базы данных."
    )
    public ResponseEntity<String> deleteVacancy(@PathVariable UUID vacancyId) {
        return ResponseEntity.ok(vacancyService.deleteById(vacancyId));
    }

    @PatchMapping("/{vacancyId}")
    @Operation(
            summary = "Изменить статус вакансии",
            description = "Обновление статуса вакансии по её ID (например, активировать или деактивировать вакансию)."
    )
    public ResponseEntity<String> changeVacancyStatus(@PathVariable UUID vacancyId, @RequestParam Status status) {
        return ResponseEntity.ok(vacancyService.changeStatus(vacancyId, status));
    }
}
