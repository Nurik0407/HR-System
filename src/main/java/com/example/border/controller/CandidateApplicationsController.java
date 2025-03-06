package com.example.border.controller;

import com.example.border.model.dto.applicant.ApplicantDto;
import com.example.border.model.dto.applicant.ApplicantsResponse;
import com.example.border.model.dto.employer.candidate.VacancyCandidatesResponse;
import com.example.border.model.dto.vacancyApplication.InviteInterviewRequest;
import com.example.border.model.dto.vacancyApplication.PositionResponse;
import com.example.border.model.enums.*;
import com.example.border.service.ApplicantService;
import com.example.border.service.VacancyApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vacancy/applicants")
@Tag(
        name = "API для управления кандидатами ",
        description = "API для работы с кандидатами на вакансии. (Соискатели)"
)
public class CandidateApplicationsController {

    private final VacancyApplicationService vacancyApplicationService;
    private final ApplicantService applicantService;

    public CandidateApplicationsController(VacancyApplicationService vacancyApplicationService, ApplicantService applicantService) {
        this.vacancyApplicationService = vacancyApplicationService;
        this.applicantService = applicantService;
    }

    @GetMapping("/responded")
    @Operation(
            summary = "Получение списка кандидатов",
            description = "Возвращает страницу с кандидатами на вакансии. " +
                    "Поддерживает фильтрацию по ключевым словам, опыту, статусу заявки и дате."
    )
    public ResponseEntity<Page<VacancyCandidatesResponse>> getCandidatesInApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyWord,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String dateFilter
    ) {
        return ResponseEntity.ok(vacancyApplicationService.getCandidates(
                page, size, keyWord, experience, status, dateFilter
        ));
    }

    @PatchMapping("/{applicationId}")
    @Operation(
            summary = "Обновление статуса заявки",
            description = "Обновляет статус заявки кандидата. " +
                    "Дополнительно можно указать сообщение для кандидата при статусе INVITED_TO_INTERVIEW."
    )
    public ResponseEntity<String> updateApplicationStatus(
            @PathVariable UUID applicationId,
            @RequestParam ApplicationStatus newStatus,
            @RequestBody InviteInterviewRequest request) {
        return ResponseEntity.ok(vacancyApplicationService.updateStatus(applicationId, newStatus, request));
    }

    @GetMapping("{applicantId}")
    @Operation(
            summary = "Получение данных соискателя по ID",
            description = "Возвращает подробные данные соискателя по его идентификатору."
    )
    public ResponseEntity<ApplicantDto> getApplicantCandidateById(@PathVariable UUID applicantId) {
        return ResponseEntity.ok(applicantService.getApplicantById(applicantId));
    }

    @GetMapping("/{applicantId}/position")
    @Operation(
            summary = "Получение позиций, на которые откликнулся соискатель",
            description = "Возвращает список позиций (вакансий), на которые откликнулся соискатель с указанным ID."
    )
    public ResponseEntity<List<PositionResponse>> getPositionApplicant(@PathVariable UUID applicantId) {
        return ResponseEntity.ok(vacancyApplicationService.getPositionByApplicant(applicantId));
    }

    @GetMapping
    @Operation(
            summary = "Получение списка соискателей",
            description = "Возвращает страницу с соискателями (кандидатами). " +
                    "Поддерживает фильтрацию по ключевым словам, позиции, образование итд."
    )
    public ResponseEntity<Page<ApplicantsResponse>> getApplicants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyWord,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) EducationLevel educationLevel,
            @RequestParam(required = false) Country country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String experience
    ) {

        return ResponseEntity.ok(applicantService.getApplicants(
                page,
                size,
                keyWord,
                position,
                educationLevel,
                country,
                city,
                experience
        ));
    }

}
