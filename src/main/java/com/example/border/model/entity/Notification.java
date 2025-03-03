package com.example.border.model.entity;

import com.example.border.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Notification extends BaseEntity {

    private String message;
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private Employer employer;

    @ManyToOne
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;
}
