package com.example.border.service.impl;

import com.example.border.config.jwt.JwtTokenUtil;
import com.example.border.exception.NotFoundException;
import com.example.border.model.dto.employer.VacanciesResponseForEmployer;
import com.example.border.model.dto.employer.VacancyDto;
import com.example.border.model.dto.employer.VacancyResponse;
import com.example.border.model.entity.Employer;
import com.example.border.model.entity.Vacancy;
import com.example.border.model.enums.AmountType;
import com.example.border.model.enums.Status;
import com.example.border.repository.EmployerRepository;
import com.example.border.repository.VacancyRepository;
import com.example.border.service.VacancyService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VacancyServiceImpl implements VacancyService {

    private static final Logger log = LoggerFactory.getLogger(VacancyServiceImpl.class);
    private final VacancyRepository vacancyRepository;
    private final EmployerRepository employerRepository;
    private final JwtTokenUtil jwtTokenUtil;
    static final String CREATED_AT = "createdAt";
    static final String APPLICATIONS_COUNT = "applicationsCount";

    public VacancyServiceImpl(VacancyRepository vacancyRepository, EmployerRepository employerRepository, JwtTokenUtil jwtTokenUtil) {
        this.vacancyRepository = vacancyRepository;
        this.employerRepository = employerRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VacanciesResponseForEmployer> getVacancies(
            int page, int size, String sort,
            String searchQuery, Status status,
            String createdDateRange) {

        Employer currentEmployer = employerRepository.findByUserEmail(jwtTokenUtil.getCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("Employer not found"));
        log.debug("Fetching vacancies for employer: {}", currentEmployer.getId());

        Pageable pageable = createPageable(sort, page, size);
        Specification<Vacancy> specification = buildSpecification(
                currentEmployer.getId(), searchQuery, status, createdDateRange);

        return vacancyRepository.findAll(specification, pageable)
                .map(this::toVacanciesResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VacancyResponse getVacancy(UUID vacancyId) {

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));

        Employer vacancyEmployer = vacancy.getEmployer();

        return new VacancyResponse(
                vacancy.getId(),
                vacancy.getCreatedAt().toLocalDate(),
                vacancy.getPosition(),
                vacancy.isOtherPositionSelected(),
                vacancy.getOtherPosition(),
                vacancy.getApplicationsCount(),
                vacancyEmployer.getName(),
                vacancyEmployer.getLogoUrl(),
                vacancyEmployer.getAboutCompany(),
                vacancy.getVacancyDescription(),
                vacancy.getRequiredSkills(),
                vacancy.getContactInformation(),
                vacancy.getAdditionalInfo(),
                vacancy.getStatus(),
                vacancy.getCountry().toString() + ", " + vacancy.getCity(),
                vacancy.getIndustry(),
                vacancy.getEmploymentType(),
                vacancy.getExperience(),
                vacancy.getAmountType(),
                vacancy.getFixedAmount(),
                vacancy.getMaxAmount(),
                vacancy.getMinAmount(),
                vacancy.getCurrency()
        );
    }

    @Override
    @Transactional
    public String createVacancy(VacancyDto request) {
        Employer currentEmployer = employerRepository.findByUserEmail(jwtTokenUtil.getCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("Employer not found"));
        log.info("Creating new vacancy for employer: {}", currentEmployer.getId());

        Vacancy newVacancy = new Vacancy(
                request.position(),
                request.isOtherPositionSelected(),
                request.otherPosition(),
                request.industry(),
                request.vacancyDescription(),
                request.requiredSkills(),
                request.amountType(),
                request.fixedAmount(),
                request.maxAmount(),
                request.minAmount(),
                request.currency(),
                request.employmentType(),
                request.experience(),
                request.country(),
                request.city(),
                request.contactInformation(),
                request.additionalInfo()
        );
        newVacancy.setStatus(Status.ACTIVE);

        newVacancy.setEmployer(currentEmployer);
        currentEmployer.getVacancies().add(newVacancy);
        employerRepository.save(currentEmployer);

        log.info("Vacancy created successfully. ID: {}", newVacancy.getId());
        return "Vacancy successfully created";
    }

    @Override
    @Transactional
    public VacancyDto updateVacancy(UUID vacancyId, VacancyDto vacancyDto) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));

        if (vacancyDto.position() != null) {
            vacancy.setPosition(vacancyDto.position());
        }
        if (vacancyDto.isOtherPositionSelected()) {
            vacancy.setOtherPosition(vacancyDto.otherPosition());
        } else {
            vacancy.setOtherPosition(null);
        }

        vacancy.setIndustry(vacancyDto.industry());
        vacancy.setVacancyDescription(vacancyDto.vacancyDescription());
        vacancy.setRequiredSkills(vacancyDto.requiredSkills());
        vacancy.setAmountType(vacancyDto.amountType());

        if (vacancyDto.amountType() == AmountType.FIXED) {
            vacancy.setFixedAmount(vacancyDto.fixedAmount());
            vacancy.setMinAmount(null);
            vacancy.setMaxAmount(null);
        } else if (vacancyDto.amountType() == AmountType.FROM) {
            vacancy.setFixedAmount(null);
            vacancy.setMaxAmount(null);
            vacancy.setMinAmount(vacancyDto.minAmount());
        } else if (vacancyDto.amountType() == AmountType.RANGE) {
            vacancy.setFixedAmount(null);
            vacancy.setMaxAmount(vacancyDto.maxAmount());
            vacancy.setMinAmount(vacancyDto.minAmount());
        }

        vacancy.setCurrency(vacancyDto.currency());
        vacancy.setEmploymentType(vacancyDto.employmentType());
        vacancy.setExperience(vacancyDto.experience());
        vacancy.setCountry(vacancyDto.country());
        vacancy.setCity(vacancyDto.city());
        vacancy.setContactInformation(vacancyDto.contactInformation());
        vacancy.setAdditionalInfo(vacancyDto.additionalInfo());

        vacancyRepository.save(vacancy);

        log.info("Vacancy updated. ID: {}", vacancyId);
        return new VacancyDto(
                vacancy.getPosition(),
                vacancy.isOtherPositionSelected(),
                vacancy.getOtherPosition(),
                vacancy.getIndustry(),
                vacancy.getVacancyDescription(),
                vacancy.getRequiredSkills(),
                vacancy.getAmountType(),
                vacancy.getFixedAmount(),
                vacancy.getMaxAmount(),
                vacancy.getMinAmount(),
                vacancy.getCurrency(),
                vacancy.getEmploymentType(),
                vacancy.getExperience(),
                vacancy.getCountry(),
                vacancy.getCity(),
                vacancy.getContactInformation(),
                vacancy.getAdditionalInfo()
        );
    }

    @Override
    @Transactional
    public String deleteById(UUID vacancyId) {
        log.info("Deleting vacancy. ID: {}", vacancyId);
        vacancyRepository.deleteById(vacancyId);
        return "Vacancy with id: " + vacancyId + " successfully deleted";
    }

    @Override
    public String changeStatus(UUID vacancyId, Status status) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));

        vacancy.setStatus(status);
        vacancyRepository.save(vacancy);
        log.info("Status changed to {} for vacancy ID: {}", status, vacancyId);
        return "Status changed successfully";
    }

    private Pageable createPageable(String sort, int page, int size) {
        Sort sortConfig;

        if (sort != null && !sort.isBlank()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String fieldName = sortParts[0];
                Sort.Direction direction = Sort.Direction.fromString(sortParts[1]);

                // сортировка только по "createdAt" and "applicationsCount"
                if (CREATED_AT.equals(fieldName) || APPLICATIONS_COUNT.equals(fieldName)) {
                    sortConfig = Sort.by(direction, fieldName);
                } else {
                    throw new IllegalArgumentException("Invalid sort field: " + fieldName);
                }
            } else {
                throw new IllegalArgumentException("Sort parameter must be in format 'field,direction'");
            }
        } else {
            sortConfig = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return PageRequest.of(page, size, sortConfig);
    }


    private Specification<Vacancy> buildSpecification(
            UUID employerId,
            String searchQuery,
            Status status,
            String createdDateRange) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (employerId != null) {
                predicates.add(cb.equal(root.get("employer").get("id"), employerId));
            }

            if (StringUtils.hasText(searchQuery)) {
                predicates.add(cb.or(
                        cb.like(root.get("position"), "%" + searchQuery + "%"),
                        cb.like(root.get("otherPosition"), "%" + searchQuery + "%")
                ));
            }

            if (status != null) {
                predicates.add(root.get("status").in(status));
            }

            if (createdDateRange != null) {
                LocalDate now = LocalDate.now();
                LocalDate startDate = switch (createdDateRange) {
                    case "today" -> now;
                    case "3days" -> now.minusDays(3);
                    case "week" -> now.minusWeeks(1);
                    default -> throw new IllegalArgumentException("Invalid date range");
                };

                predicates.add(cb.between(
                        root.get("createdAt"),
                        startDate.atStartOfDay(),
                        LocalDateTime.now()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private VacanciesResponseForEmployer toVacanciesResponse(Vacancy vacancy) {
        return new VacanciesResponseForEmployer(
                vacancy.getId(),
                vacancy.getPosition() == null ?
                        vacancy.getOtherPosition() : vacancy.getPosition().toString(),
                vacancy.getEmploymentType(),
                vacancy.getAmountType(),
                vacancy.getFixedAmount(),
                vacancy.getMinAmount(),
                vacancy.getMaxAmount(),
                vacancy.getExperience(),
                vacancy.getApplicationsCount(),
                vacancy.getCreatedAt(),
                vacancy.getStatus()
        );
    }
}
