package com.example.border.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class VerificationCode extends BaseEntity {

    @Column(nullable = false,unique = true)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public VerificationCode (User user,String code, LocalDateTime expirationTime) {
        this.user = user;
        this.code = code;
        this.expirationTime = expirationTime;
    }
}
