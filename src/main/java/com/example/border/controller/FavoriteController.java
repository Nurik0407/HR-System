package com.example.border.controller;

import com.example.border.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "API для управления избранными", description = "API для добавления, удаления и проверки избранных кандидатов")
public class FavoriteController {

    private final FavoriteService favoriteService;


    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{applicantId}")
    @Operation(
            summary = "Добавить кандидата в избранное",
            description = "Добавляет кандидата в список избранных для текущего работодателя"
    )
    public ResponseEntity<String> addFavorite(@PathVariable UUID applicantId) {
        return ResponseEntity.ok(favoriteService.addFavorite(applicantId));
    }

    @DeleteMapping("/{applicantId}")
    @Operation(
            summary = "Удалить кандидата из избранного",
            description = "Удаляет кандидата из списка избранных для текущего работодателя"
    )
    public ResponseEntity<String> removeFavorite(@PathVariable UUID applicantId) {
        return ResponseEntity.ok(favoriteService.removeFavorite(applicantId));
    }

    @GetMapping("/check/{applicantId}")
    @Operation(
            summary = "Проверить наличие кандидата в избранном",
            description = "Возвращает true, если кандидат в списке избранных, иначе false"
    )
    public ResponseEntity<Boolean> isApplicantInFavorites(@PathVariable UUID applicantId) {
        return ResponseEntity.ok(favoriteService.isApplicantInFavorites(applicantId));
    }
}
