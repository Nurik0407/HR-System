package com.example.border.model.enums;

import lombok.Getter;

@Getter
public enum EducationDegree {
    BACHELOR("бакалавриат"),
    MASTER("магистратура");

    private final String description;

    EducationDegree(String description) {
        this.description = description;
    }
}
