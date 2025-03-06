package com.example.border.model.dto.applicant;

import com.example.border.model.enums.Country;
import com.example.border.model.enums.Experience;
import com.example.border.model.enums.Position;

import java.util.UUID;

public record ApplicantsResponse(
        UUID applicantId,
        String profileImageUrl,
        String firstName,
        String lastName,
        Position position,
        Experience experience,
        Country country,
        String city,
        boolean inFavorite
) {
}
