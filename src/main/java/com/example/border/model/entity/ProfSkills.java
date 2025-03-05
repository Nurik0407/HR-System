package com.example.border.model.entity;

import com.example.border.model.enums.Experience;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ProfSkills extends BaseEntity {

    private String aboutMe;
    @Column(name = "cv_url")
    private String CVUrl;
    private Experience experience;

    @OneToOne(mappedBy = "profSkills")
    private Applicant applicant;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "prof_skills_id")
    private List<Education> educationList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "prof_skills_id")
    private List<WorkExperience> workExperiences = new ArrayList<>();
}
