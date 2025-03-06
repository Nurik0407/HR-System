package com.example.border.repository;

import com.example.border.model.entity.ResetToken;
import com.example.border.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResetTokenRepository extends JpaRepository<ResetToken, UUID> {
    Optional<ResetToken> findByToken(String token);

    Optional<ResetToken> findByUser(User user);
}