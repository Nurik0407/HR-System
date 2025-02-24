package com.example.border.repository;

import com.example.border.model.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, UUID> {

    Optional<Applicant> findApplicantByUserEmail(String email);
}