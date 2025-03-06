package com.example.border.controller;

import com.example.border.model.dto.employer.EmployerDto;
import com.example.border.service.EmployerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employers")
@Tag(name = "Работодатели", description = "API для работы с данными работодателей")
public class EmployerController {

    private final EmployerService employerService;

    public EmployerController(EmployerService employerService) {
        this.employerService = employerService;
    }

    @GetMapping
    @Operation(
            summary = "Получение данных работодателя",
            description = "Возвращает данные текущего работодателя."
    )
    public ResponseEntity<EmployerDto> getProfile(){
        return ResponseEntity.ok(employerService.getProfile());
    }

    @PutMapping
    @Operation(
            summary = "Обновление данных работодателя",
            description = "Обновляет информацию текущего работодателя."
    )
    public ResponseEntity<EmployerDto> updateProfile(@Valid @RequestBody EmployerDto employerDto){
        return ResponseEntity.ok(employerService.updateProfile(employerDto));
    }
}
