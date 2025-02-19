package com.example.border.service.impl;

import com.example.border.config.jwt.JwtTokenUtil;
import com.example.border.exception.NotFoundException;
import com.example.border.exception.UnauthorizedAccessException;
import com.example.border.model.dto.employer.VacanciesResponseForEmployer;
import com.example.border.model.dto.employer.VacancyDto;
import com.example.border.model.dto.employer.VacancyResponse;
import com.example.border.model.dto.vacancy.SimilarVacanciesResponse;
import com.example.border.model.dto.vacancy.VacanciesResponse;
import com.example.border.model.entity.Employer;
import com.example.border.model.entity.Vacancy;
import com.example.border.model.enums.*;
import com.example.border.repository.EmployerRepository;
import com.example.border.repository.VacancyRepository;
import com.example.border.service.VacancyService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Page<VacanciesResponseForEmployer> getVacanciesForCurrentEmployer(
            int page, int size, String sort,
            String searchQuery, Status status,
            String createdDateRange) {

        Employer currentEmployer = currentEmployer();
        log.debug("Fetching vacancies for employer: {}", currentEmployer.getId());

        Pageable pageable = createPageableEmployer(sort, page, size);
        Specification<Vacancy> specification = buildSpecification(
                currentEmployer.getId(), searchQuery, status, createdDateRange);

        return vacancyRepository.findAll(specification, pageable)
                .map(this::toEmployerVacancyResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VacancyResponse getVacancyForCurrentEmployer(UUID vacancyId) {

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));
        Employer vacancyEmployer = vacancy.getEmployer();

        checkIfUserHasAccessToVacancy(vacancyEmployer.getId(), currentEmployer().getId());

        return toVacancyResponse(vacancy);
    }

    @Override
    @Transactional
    public String createVacancy(VacancyDto request) {
        Employer currentEmployer = currentEmployer();
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

        checkIfUserHasAccessToVacancy(vacancy.getEmployer().getId(), currentEmployer().getId());

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
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));

        checkIfUserHasAccessToVacancy(vacancy.getEmployer().getId(), currentEmployer().getId());

        log.info("Deleting vacancy. ID: {}", vacancyId);
        vacancyRepository.deleteById(vacancyId);
        return "Vacancy with id: " + vacancyId + " successfully deleted";
    }

    @Override
    public String changeStatus(UUID vacancyId, Status status) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));

        checkIfUserHasAccessToVacancy(vacancy.getEmployer().getId(), currentEmployer().getId());

        vacancy.setStatus(status);
        vacancyRepository.save(vacancy);
        log.info("Status changed to {} for vacancy ID: {}", status, vacancyId);
        return "Status changed successfully";
    }

    @Override
    public Page<VacanciesResponse> getActiveVacancies(
            String searchQuery,
            Industry industry,
            Position position,
            Country country,
            String city,
            Experience experience,
            EmploymentType employmentType,
            String createdAtSort,
            String amountSort,
            int page,
            int size) {

        log.info("Fetching vacancies with filters: searchQuery={}, industry={}, position={}, country={}, city={}, experience={}, employmentType={}, createdAtSort={}, amountSort={}, page={}, size={}",
                searchQuery, industry, position, country, city, experience, employmentType, createdAtSort, amountSort, page, size);

        Specification<Vacancy> specification = buildSpecificationVacancies(searchQuery, industry,
                position, country, city, experience, employmentType);


        Pageable pageable = createPageable(createdAtSort, amountSort, page, size);

        return vacancyRepository.findAll(specification, pageable)
                .map(this::toVacanciesResponse);
    }

    @Override
    public VacancyResponse getVacancy(UUID vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));
        return toVacancyResponse(vacancy);
    }

    @Override
    public Page<SimilarVacanciesResponse> findSimilarVacancies(UUID vacancyId, int size, int page) {
        log.info("Starting to find similar vacancies for vacancy ID: {}", vacancyId);
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));

        List<Vacancy> filteredVacancies = vacancyRepository.getVacanciesByPositionOrOtherPosition(
                        vacancy.getPosition(),
                        vacancy.getOtherPosition()).stream()
                .filter(v -> !v.getId().equals(vacancyId))
                .collect(Collectors.toList());

        log.info("Found {} vacancies after filtering by position", filteredVacancies.size());

        if (filteredVacancies.size() <= 2) {
            return createPage(filteredVacancies, page, size);
        }

        filteredVacancies = filteredVacancies.stream()
                .filter(v -> isSimilarByAmount(vacancy, v))
                .collect(Collectors.toList());

        if (filteredVacancies.size() <= 2) {
            return createPage(filteredVacancies, page, size);
        }

        filteredVacancies = filteredVacancies.stream()
                .filter(v -> v.getEmploymentType() == vacancy.getEmploymentType())
                .collect(Collectors.toList());

        if (filteredVacancies.size() <= 2) {
            return createPage(filteredVacancies, page, size);
        }

        filteredVacancies = filteredVacancies.stream()
                .filter(v -> v.getCountry() == vacancy.getCountry())
                .collect(Collectors.toList());

        return createPage(filteredVacancies, page, size);
    }

    private Page<SimilarVacanciesResponse> createPage(List<Vacancy> filteredVacancies, int page, int size) {
        log.info("Creating page with {} vacancies for page number: {} and size: {}", filteredVacancies.size(), page, size);
        List<SimilarVacanciesResponse> responseList = filteredVacancies.stream()
                .map(this::mapToSimilarVacanciesResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, PageRequest.of(page, size), filteredVacancies.size());
    }

    private Pageable createPageable(String createdAtSort, String amountSort, int page, int size) {
        log.info("Creating pageable object with sorting: createdAtSort={}, amountSort={}, page={}, size={}",
                createdAtSort, amountSort, page, size);

        List<Sort.Order> orders = new ArrayList<>();

        if (StringUtils.hasText(createdAtSort)) {
            String[] params = createdAtSort.split(",");
            if (params.length == 2) {
                orders.add(new Sort.Order("asc".equalsIgnoreCase(params[1]) ? Sort.Direction.ASC : Sort.Direction.DESC, params[0]));
            } else {
                log.warn("Invalid createdAtSort parameter: {}", createdAtSort);
                throw new IllegalArgumentException("Invalid createdAtSort parameter: " + createdAtSort);
            }
        }

        if (StringUtils.hasText(amountSort)) {
            String[] params = amountSort.split(",");
            if (params.length == 2) {
                orders.add(new Sort.Order("asc".equalsIgnoreCase(params[1]) ? Sort.Direction.ASC : Sort.Direction.DESC, params[0]));
            } else {
                log.warn("Invalid amountSort parameter: {}", amountSort);
                throw new IllegalArgumentException("Invalid amountSort parameter: " + amountSort);
            }
        }

        return PageRequest.of(page, size, Sort.by(orders));
    }

    private Specification<Vacancy> buildSpecificationVacancies(
            String searchQuery,
            Industry industry,
            Position position,
            Country country,
            String city,
            Experience experience,
            EmploymentType employmentType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            log.info("Building vacancy specifications with filters");

            predicates.add(cb.equal(root.get("status"), Status.ACTIVE));

            if (StringUtils.hasText(searchQuery)) {
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("position")), searchPattern),
                        cb.like(cb.lower(root.get("otherPosition")), searchPattern),
                        cb.like(cb.lower(root.get("employer").get("name")), searchPattern),
                        cb.like(cb.lower(root.get("industry")), searchPattern)
                ));
            }

            if (industry != null) {
                predicates.add(cb.equal(root.get("industry"), industry));
            }

            if (position != null) {
                predicates.add(cb.equal(root.get("position"), position));
            }

            if (country != null) {
                predicates.add(cb.equal(root.get("country"), country));
            }

            if (StringUtils.hasText(city)) {
                predicates.add(cb.equal(root.get("city"), city));
            }

            if (experience != null) {
                predicates.add(cb.equal(root.get("experience"), experience));
            }

            if (employmentType != null) {
                predicates.add(cb.equal(root.get("employmentType"), employmentType));
            }

            log.info("Built {} predicates for vacancy search", predicates.size());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable createPageableEmployer(String sort, int page, int size) {
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
                predicates.add(cb.equal(root.get("status"), status));
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

    private VacanciesResponseForEmployer toEmployerVacancyResponse(Vacancy vacancy) {
        return new VacanciesResponseForEmployer(
                vacancy.getId(),
                vacancy.isOtherPositionSelected() ?
                        vacancy.getOtherPosition() : Objects.requireNonNull(vacancy.getPosition()).toString(),
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

    private VacanciesResponse toVacanciesResponse(Vacancy vacancy) {
        return new VacanciesResponse(
                vacancy.getEmployer().getName(),
                vacancy.getEmployer().getLogoUrl(),
                vacancy.getId(),
                vacancy.getCountry(),
                vacancy.getCity(),
                vacancy.isOtherPositionSelected() ?
                        vacancy.getOtherPosition() : Objects.requireNonNull(vacancy.getPosition()).toString(),
                vacancy.getIndustry(),
                vacancy.getAmountType(),
                vacancy.getFixedAmount(),
                vacancy.getMaxAmount(),
                vacancy.getMinAmount(),
                vacancy.getCurrency(),
                vacancy.getEmploymentType(),
                vacancy.getExperience()
        );
    }

    private VacancyResponse toVacancyResponse(Vacancy vacancy) {
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

    private Employer currentEmployer() {
        return employerRepository.findByUserEmail(jwtTokenUtil.getCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("Employer not found"));
    }

    private void checkIfUserHasAccessToVacancy(UUID vacancyEmployerId, UUID currentEmployerId) {
        if (!currentEmployerId.equals(vacancyEmployerId)) {
            throw new UnauthorizedAccessException("You are not authorized to access this vacancy.");
        }
    }

    private boolean isSimilarByAmount(Vacancy vacancy, Vacancy other) {
        BigDecimal thisAmount = vacancy.getAmount() != null ? BigDecimal.valueOf(vacancy.getAmount()) : BigDecimal.ZERO;
        BigDecimal otherAmount = other.getAmount() != null ? BigDecimal.valueOf(other.getAmount()) : BigDecimal.ZERO;

        // Разница в зарплате не должна превышать 20%
        BigDecimal lowerBound = thisAmount.multiply(BigDecimal.valueOf(0.8));
        BigDecimal upperBound = thisAmount.multiply(BigDecimal.valueOf(1.2));

        return otherAmount.compareTo(lowerBound) >= 0 && otherAmount.compareTo(upperBound) <= 0;
    }

    private SimilarVacanciesResponse mapToSimilarVacanciesResponse(Vacancy vacancy) {
        return new SimilarVacanciesResponse(
                vacancy.getId(),
                vacancy.getEmployer().getName(),
                vacancy.getPosition() != null ? vacancy.getPosition().name() : vacancy.getOtherPosition(),
                vacancy.getAmountType(),
                vacancy.getFixedAmount(),
                vacancy.getMinAmount(),
                vacancy.getMaxAmount(),
                vacancy.getEmploymentType(),
                vacancy.getCountry(),
                vacancy.getCity()
        );
    }
}
