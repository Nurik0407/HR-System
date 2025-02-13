package com.example.border.repository;

import com.example.border.model.entity.User;
import com.example.border.model.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    void deleteByUser(User user);

    Optional<VerificationCode> findByUser(User user);
}