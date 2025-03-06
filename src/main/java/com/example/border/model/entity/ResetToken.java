package com.example.border.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ResetToken extends BaseEntity{

    private String token;
    private LocalDateTime expiryTime;

    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    public ResetToken(String token, LocalDateTime expiryTime, User user) {
        this.token = token;
        this.expiryTime = expiryTime;
        this.user = user;
    }
}
