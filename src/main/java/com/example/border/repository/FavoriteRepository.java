package com.example.border.repository;

import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.Employer;
import com.example.border.model.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    boolean existsByEmployerAndApplicant(Employer employer, Applicant applicant);

    void deleteFavoriteByApplicantAndEmployer(Applicant applicant, Employer employer);
}