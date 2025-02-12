package com.example.border.model.enums;

import lombok.Getter;

@Getter
public enum Experience {
    NO_PREFERENCE("Не имеет значения"),
    NO_EXPERIENCE("Без опыта"),
    ONE_TO_THREE_YEARS("От 1 года до 3 лет"),
    THREE_TO_SIX_YEARS("От 3 лет до 6 лет"),
    MORE_THAN_SIX_YEARS("Более 6 лет");

    private final String description;

    Experience(String description) {
        this.description = description;
    }
}
