package com.example.border.model.enums;

import lombok.Getter;

@Getter
public enum EducationLevel {
    SECONDARY("Среднее образование"),
    BACHELOR("Бакалавр"),
    MASTER("Магистр"),
    CANDIDATE("Кандидат наук"),
    DOCTOR("Доктор наук");

    private final String description;

    EducationLevel(String description) {
        this.description = description;
    }
}
