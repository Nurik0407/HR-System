package com.example.border.repository;

import com.example.border.model.entity.VacancyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VacancyApplicationRepository extends JpaRepository<VacancyApplication, UUID> {
    boolean existsByApplicant_IdAndVacancy_Id(UUID applicantId, UUID vacancyId);
}