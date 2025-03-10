package com.example.border.controller;

import com.example.border.model.dto.applicant.ApplicantDto;
import com.example.border.model.entity.User;
import com.example.border.service.ApplicantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applicant")
@Tag(name = "Соискатели", description = "API для работы с данными соискателей")
public class ApplicantController {

    private final ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    @GetMapping
    @Operation(
            summary = "Получение данных соискателя",
            description = "Возвращает данные текущего соискателя."
    )
    public ResponseEntity<ApplicantDto> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        return ResponseEntity.ok(applicantService.getApplicantById(user.getApplicant().getId()));
    }

    @PutMapping
    @Operation(
            summary = "Обновление данных соискателя",
            description = "Обновляет информацию текущего соискателя."
    )
    public ResponseEntity<ApplicantDto> updateApplicant(
            @Valid @RequestBody ApplicantDto applicantDto) {
        return ResponseEntity.ok(applicantService.updateCurrentApplicant(applicantDto));
    }
}
