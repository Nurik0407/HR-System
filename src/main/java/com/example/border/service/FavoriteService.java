package com.example.border.service;

import java.util.UUID;

public interface FavoriteService {
    String addFavorite(UUID applicantId);

    String removeFavorite(UUID applicantId);

    Boolean isApplicantInFavorites(UUID applicantId);
}
