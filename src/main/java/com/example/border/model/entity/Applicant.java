package com.example.border.model.entity;

import com.example.border.model.enums.Country;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Applicant extends BaseEntity {

    private String firstName;
    private String lastName;

    private LocalDate birthDay;
    @Enumerated(EnumType.STRING)
    private Country country;
    private String city;
    private String address;
    private String phoneNumber;
    private String profilePhotoUrl;

    @Formula("CONCAT(first_name,' ',last_name)")
    private String fullName;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinColumn(name = "prof_skills_id")
    private ProfSkills profSkills;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL)
    private List<VacancyApplication> vacancyApplications;

    @OneToMany(mappedBy = "applicant")
    private List<Notification> notifications = new ArrayList<>();
}
