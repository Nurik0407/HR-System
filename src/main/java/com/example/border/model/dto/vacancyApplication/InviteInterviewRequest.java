package com.example.border.model.dto.vacancyApplication;

public record InviteInterviewRequest(
        String message,
        String agreementUrl
) {
    public InviteInterviewRequest(String message, String agreementUrl) {
        this.message = message;
        this.agreementUrl = message != null ? agreementUrl : null;
    }
}
