package com.example.border.controller;

import com.example.border.model.dto.vacancyApplication.ApplicantApplicationsResponse;
import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;
import com.example.border.model.enums.ApplicationStatus;
import com.example.border.service.VacancyApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vacancy/applications")
@Tag(name = "Отклик на вакансию", description = "API для отклика на вакансий для соискателей")
public class VacancyApplicationController {

    private final VacancyApplicationService applicationService;


    public VacancyApplicationController(VacancyApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/{vacancyId}/apply")
    @Operation(
            summary = "Отклик на вакансию",
            description = "Позволяет соискателю откликнуться на конкретную вакансию, отправив заявку на рассмотрение."
    )
    public ResponseEntity<String> applyForVacancy(@PathVariable UUID vacancyId,
                                                  @RequestBody VacancyApplicationRequest request) {
        return ResponseEntity.ok(applicationService.applyForVacancy(vacancyId, request));
    }

    @GetMapping
    @Operation(
            summary = "Получить отклики пользователя",
            description = "Возвращает список откликов соискателя с поиском, фильтрацией по дате и статусу, а также пагинацией"
    )
    public ResponseEntity<Page<ApplicantApplicationsResponse>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String dateFilter
    ) {
        return ResponseEntity.ok(applicationService.getMyApplications(page, size, keyword, status, dateFilter));
    }
}
