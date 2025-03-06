package com.example.border.service.impl;

import com.example.border.config.jwt.JwtTokenUtil;
import com.example.border.exception.NotFoundException;
import com.example.border.model.dto.applicant.ApplicantDto;
import com.example.border.model.dto.applicant.EducationDto;
import com.example.border.model.dto.applicant.WorkExperienceDto;
import com.example.border.model.dto.employer.candidate.ExperiencePeriod;
import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.Education;
import com.example.border.model.entity.ProfSkills;
import com.example.border.model.entity.WorkExperience;
import com.example.border.model.enums.Experience;
import com.example.border.repository.ApplicantRepository;
import com.example.border.service.ApplicantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.border.model.enums.Experience.*;

@Service
public class ApplicantServiceImpl implements ApplicantService {

    private static final Logger log = LoggerFactory.getLogger(ApplicantServiceImpl.class);
    private final ApplicantRepository applicantRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public ApplicantServiceImpl(ApplicantRepository applicantRepository, JwtTokenUtil jwtTokenUtil) {
        this.applicantRepository = applicantRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional(readOnly = true)
    @Override
    public ApplicantDto getApplicantById(UUID id) {

        log.debug("Fetching applicant by ID: {}", id);
        Applicant applicant = findApplicantById(id);

        log.info("Applicant found: {}", applicant.getId());
        return convertToResponse(applicant);
    }

    @Transactional
    @Override
    public ApplicantDto updateCurrentApplicant(ApplicantDto newApplicant) {
        String currentUserEmail = jwtTokenUtil.getCurrentUserEmail();

        log.debug("Updating applicant with email: {}", currentUserEmail);
        Applicant applicant = applicantRepository.findApplicantByUserEmail(currentUserEmail)
                .orElseThrow(() -> new NotFoundException("Applicant with email " + currentUserEmail + " not found"));

        applicant.setFirstName(newApplicant.firstName());
        applicant.setLastName(newApplicant.lastName());
        applicant.setProfilePhotoUrl(newApplicant.profilePhotoUrl());
        applicant.setBirthDay(newApplicant.birthDay());
        applicant.setCountry(newApplicant.country());
        applicant.setCity(newApplicant.city());
        applicant.setAddress(newApplicant.address());
        applicant.setPhoneNumber(newApplicant.phoneNumber());

        ProfSkills profSkills = applicant.getProfSkills();
        if (applicant.getProfSkills() == null) {
            profSkills = new ProfSkills();
            applicant.setProfSkills(profSkills);
        }
        profSkills.setAboutMe(newApplicant.aboutMe());
        profSkills.setCVUrl(newApplicant.CVUrl());

        updateEducations(applicant, newApplicant.educationsResponse());
        updateWorkExperiences(applicant, newApplicant.workExperiencesResponse());

        Applicant savedApplicant = applicantRepository.save(applicant);
        return convertToResponse(savedApplicant);
    }

    @Override
    public Applicant findApplicantById(UUID applicantId) {
        return applicantRepository.findApplicantById(applicantId)
                .orElseThrow(() -> {
                    log.error("Applicant not found with ID: {}", applicantId);
                    return new NotFoundException("Applicant with ID " + applicantId + " not found");
                });
    }

    private void updateEducations(Applicant applicant, List<EducationDto> educationsResponse) {
        ProfSkills skills = applicant.getProfSkills();
        List<Education> educations = skills.getEducationList();

        Map<UUID, Education> existingEducationMap = educations.stream()
                .collect(Collectors.toMap(Education::getId, Function.identity()));

        Set<UUID> receivedId = educationsResponse.stream()
                .map(EducationDto::educationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Update or add education
        educationsResponse.forEach(dto -> {
            if (dto.educationId() != null && existingEducationMap.containsKey(dto.educationId())) {
                Education existing = existingEducationMap.get(dto.educationId());
                existing.setInstitution(dto.institution());
                existing.setEducationDegree(dto.educationDegree());
                existing.setGraduationDate(dto.graduationDate());
            } else {
                Education newEducation = new Education(
                        dto.educationDegree(),
                        dto.institution(),
                        dto.graduationDate()
                );
                educations.add(newEducation);
                receivedId.add(newEducation.getId());
            }
        });
        educations.removeIf(education -> !receivedId.contains(education.getId()));
    }

    private void updateWorkExperiences(Applicant applicant, List<WorkExperienceDto> wordExperiencesResponse) {
        ProfSkills skills = applicant.getProfSkills();
        List<WorkExperience> workExperiences = skills.getWorkExperiences();

        Map<UUID, WorkExperience> existingWorkMap = workExperiences.stream()
                .collect(Collectors.toMap(WorkExperience::getId, Function.identity()));

        Set<UUID> receivedId = wordExperiencesResponse.stream()
                .map(WorkExperienceDto::workExperienceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Update or add wordExperiences
        wordExperiencesResponse.forEach(dto -> {
            if (dto.workExperienceId() != null && existingWorkMap.containsKey(dto.workExperienceId())) {
                WorkExperience existing = existingWorkMap.get(dto.workExperienceId());
                existing.setPosition(dto.position());
                existing.setCompanyName(dto.companyName());
                existing.setStartDate(dto.startDate());
                existing.setEndDate(dto.endDate());
                existing.setSkills(dto.skills());
                existing.setCurrentJob(dto.currentJob());
            } else {
                WorkExperience workExperience = new WorkExperience(
                        dto.position(),
                        dto.companyName(),
                        dto.startDate(),
                        dto.endDate(),
                        dto.skills(),
                        dto.currentJob()
                );
                workExperiences.add(workExperience);
                receivedId.add(workExperience.getId());
            }
        });
        workExperiences.removeIf(workExperience -> !receivedId.contains(workExperience.getId()));

        ExperiencePeriod experiencePeriod = calculateTotalExperience(workExperiences);
        Experience experience = determineExperienceLevel(experiencePeriod.totalExperienceYears(), experiencePeriod.totalExperienceMonths());
        skills.setExperience(experience);
    }

    private ExperiencePeriod calculateTotalExperience(List<WorkExperience> workExperiences) {
        int totalExperienceYears = 0;
        int totalExperienceMonths = 0;

        for (WorkExperience workExperience : workExperiences) {
            LocalDate startDate = workExperience.getStartDate();
            LocalDate endDate = workExperience.getEndDate() != null ? workExperience.getEndDate() : LocalDate.now();

            Period period = Period.between(startDate, endDate);
            totalExperienceYears += period.getYears();
            totalExperienceMonths += period.getMonths();
        }

        totalExperienceYears += totalExperienceMonths / 12;
        totalExperienceMonths = totalExperienceMonths % 12;

        log.debug("Total experience years: {} years and {} months", totalExperienceYears, totalExperienceMonths);

        return new ExperiencePeriod(totalExperienceYears, totalExperienceMonths);
    }

    private Experience determineExperienceLevel(int totalExperienceYears, int totalExperienceMonths) {
        double totalYears = totalExperienceYears + (totalExperienceMonths / 12.0);
        log.info("Total experience years: {} years", totalYears);

        if (totalYears < 1) {
            return LESS_THAN_ONE_YEAR;
        } else if (totalYears < 3) {
            return ONE_TO_THREE_YEARS;
        } else if (totalYears < 6) {
            return THREE_TO_SIX_YEARS;
        } else {
            return MORE_THAN_SIX_YEARS;
        }
    }

    private ApplicantDto convertToResponse(Applicant applicant) {
        ProfSkills skills = applicant.getProfSkills();

        return new ApplicantDto(
                applicant.getFirstName(),
                applicant.getLastName(),
                applicant.getProfilePhotoUrl(),
                applicant.getBirthDay(),
                applicant.getCountry(),
                applicant.getCity(),
                applicant.getAddress(),
                applicant.getPhoneNumber(),
                skills != null ? skills.getAboutMe() : null,
                skills != null ? skills.getCVUrl() : null,
                skills != null ? skills.getEducationList().stream()
                        .map(education ->
                                new EducationDto(
                                        education.getId(),
                                        education.getEducationDegree(),
                                        education.getInstitution(),
                                        education.getGraduationDate()))
                        .toList() : Collections.emptyList(),
                skills != null ? skills.getWorkExperiences().stream()
                        .map(workExperience ->
                                new WorkExperienceDto(
                                        workExperience.getId(),
                                        workExperience.getPosition(),
                                        workExperience.getCompanyName(),
                                        workExperience.getStartDate(),
                                        workExperience.getEndDate(),
                                        workExperience.isCurrentJob(),
                                        workExperience.getSkills()))
                        .toList() : Collections.emptyList()
        );
    }
}
