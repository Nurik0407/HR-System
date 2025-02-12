package com.example.border.model.enums;

import lombok.Getter;

@Getter
public enum AmountType {
    FIXED("Фиксированная"),
    FROM("От"),
    RANGE("От/до");

    private final String description;
    AmountType(String description) {
        this.description = description;
    }
}
