package com.example.border.repository;

import com.example.border.model.entity.VacancyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VacancyApplicationRepository extends JpaRepository<VacancyApplication, UUID>, JpaSpecificationExecutor<VacancyApplication> {
    boolean existsByApplicant_IdAndVacancy_Id(UUID applicantId, UUID vacancyId);

    List<VacancyApplication> findAllByApplicant_IdAndVacancy_Employer_Id(UUID applicantId, UUID vacancyEmployerId);
}