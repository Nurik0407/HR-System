package com.example.border.model.entity;

import com.example.border.model.enums.Role;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user")
    Employer employer;

    @OneToOne(mappedBy = "user")
    Applicant applicant;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE,orphanRemoval = true)
    private List<VacancyApplication> vacancyApplications;
}
