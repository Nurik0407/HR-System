package com.example.border.model.entity;

import com.example.border.model.enums.Country;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Applicant extends BaseEntity {

    private String firstName;
    private String lastName;

    private LocalDateTime birthDay;
    @Enumerated(EnumType.STRING)
    private Country country;
    private String city;
    private String address;
    private String phoneNumber;
    private String profilePhotoUrl;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
