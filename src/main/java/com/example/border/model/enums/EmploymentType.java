package com.example.border.model.enums;

import lombok.Getter;

@Getter
public enum EmploymentType {
    FULL_TIME("Полная занятность"),
    PART_TIME("Частичная занятность"),
    TEMPORARY("Временная занятность"),
    FREELANCE("Фриланс/Самозанятость"),
    VOLUNTEERING("Волонтерство");
    private final String description;

    EmploymentType(String description) {
        this.description = description;
    }
}
