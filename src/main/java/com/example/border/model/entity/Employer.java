package com.example.border.model.entity;

import com.example.border.model.enums.Country;
import jakarta.persistence.*;

@Entity
public class Employer extends BaseEntity {

    private String name;
    private String aboutCompany;
    @Enumerated(EnumType.STRING)
    private Country country;
    private String city;
    private String address;
    private String phoneNumber;
    private String logoUrl;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
