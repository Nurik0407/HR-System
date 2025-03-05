package com.example.border.service.impl;

import com.example.border.config.jwt.JwtTokenUtil;
import com.example.border.exception.ApplicationAlreadySubmittedException;
import com.example.border.exception.NotFoundException;
import com.example.border.model.dto.employer.candidate.VacancyCandidatesResponse;
import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.dto.vacancyApplication.ApplicantApplicationsResponse;
import com.example.border.model.dto.vacancyApplication.InviteInterviewRequest;
import com.example.border.model.dto.vacancyApplication.PositionResponse;
import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;
import com.example.border.model.entity.*;
import com.example.border.model.enums.*;
import com.example.border.repository.ApplicantRepository;
import com.example.border.repository.VacancyApplicationRepository;
import com.example.border.repository.VacancyRepository;
import com.example.border.service.ChatRoomService;
import com.example.border.service.NotificationService;
import com.example.border.service.VacancyApplicationService;
import com.example.border.utils.UserContext;
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
import java.util.*;

@Service
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

    private static final Logger log = LoggerFactory.getLogger(VacancyApplicationServiceImpl.class);
    private final VacancyApplicationRepository applicationRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final VacancyRepository vacancyRepository;
    private final ApplicantRepository applicantRepository;
    private final NotificationService notificationService;
    private final ChatRoomService chatRoomService;
    private final UserContext userContext;

    public VacancyApplicationServiceImpl(VacancyApplicationRepository applicationRepository, JwtTokenUtil jwtTokenUtil, VacancyRepository vacancyRepository, ApplicantRepository applicantRepository, NotificationService notificationService, ChatRoomService chatRoomService, UserContext userContext) {
        this.applicationRepository = applicationRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.vacancyRepository = vacancyRepository;
        this.applicantRepository = applicantRepository;
        this.notificationService = notificationService;
        this.chatRoomService = chatRoomService;
        this.userContext = userContext;
    }

    @Override
    public String applyForVacancy(UUID vacancyId, VacancyApplicationRequest request) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new NotFoundException("Vacancy not found"));

        Applicant currentApplicant = getCurrentApplicant();
        Employer employer = vacancy.getEmployer();

        if (applicationRepository.existsByApplicant_IdAndVacancy_Id(currentApplicant.getId(), vacancyId)) {
            throw new ApplicationAlreadySubmittedException("The application has already been sent for this vacancy.");
        }

        VacancyApplication application = new VacancyApplication();
        application.setApplicationStatus(ApplicationStatus.SENT);
        application.setCoverLetter(request.coverLetter());
        application.setVacancy(vacancy);
        application.setApplicant(currentApplicant);

        applicationRepository.save(application);

        String message = currentApplicant.getFirstName() +
                " " + currentApplicant.getLastName() +
                " откликнулся(ась) на вакансию " +
                "\"" + (!vacancy.isOtherPositionSelected()
                ? vacancy.getPosition() : vacancy.getOtherPosition()) + "\"";

        notificationService.createNotification(
                null, employer, message, NotificationType.APPLICATION);

        ChatRoom chatRoom = chatRoomService.getOrCreateChat(currentApplicant, employer);

        chatRoomService.sendMessage(chatRoom.getId(),
                new MessageRequest(
                        currentApplicant.getUser().getId(),
                        employer.getUser().getId(),
                        request.coverLetter(),
                        MessageType.TEXT
                ));

        chatRoomService.sendMessage(chatRoom.getId(),
                new MessageRequest(
                        currentApplicant.getUser().getId(),
                        employer.getUser().getId(),
                        request.CVUrl(),
                        MessageType.FILE
                ));

        return "The application is successfully sent";
    }

    @Override
    public Page<ApplicantApplicationsResponse> getMyApplications(
            int page,
            int size,
            String keyword,
            ApplicationStatus status,
            String dateFilter) {

        Applicant currentApplicant = getCurrentApplicant();
        log.info("Fetching applications for applicant: {}, page: {}, size: {}, keyword: {}, status: {}, dateFilter: {}",
                currentApplicant.getId(), page, size, keyword, status, dateFilter);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<VacancyApplication> specification = buildSpecificationApplication(
                keyword, status, dateFilter, currentApplicant.getId()
        );

        Page<VacancyApplication> vacancyApplications = applicationRepository.findAll(specification, pageable);

        log.info("Fetched {} applications for user: {}", vacancyApplications.getTotalElements(), currentApplicant.getId());

        return vacancyApplications.map(this::toResponse);
    }

    @Transactional
    @Override
    public Page<VacancyCandidatesResponse> getCandidates(
            int page,
            int size,
            String keyword,
            String experience,
            ApplicationStatus status,
            String dateFilter) {

        Employer currentEmployer = userContext.getCurrentUser().getEmployer();
        log.info("Fetching candidates for employer: {}", currentEmployer.getId());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<VacancyApplication> specification = buildSpecificationEmployer(
                currentEmployer.getId(),
                keyword,
                experience,
                status,
                dateFilter
        );

        Page<VacancyApplication> vacancyApplicationsCandidate = applicationRepository.findAll(specification, pageable);

        return vacancyApplicationsCandidate.map(this::mapToVacancyCandidateResponse);
    }

    @Override
    public String updateStatus(UUID applicationId, ApplicationStatus newStatus, InviteInterviewRequest request) {
        log.info("Updating status for applicationId: {}", applicationId);

        VacancyApplication vacancyApplication = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        ApplicationStatus currentStatus = vacancyApplication.getApplicationStatus();
        log.info("Current status: {}. New status: {}", currentStatus, newStatus);

        if (newStatus == ApplicationStatus.INVITED_TO_INTERVIEW && !StringUtils.hasText(request.message()) && request.agreementUrl() != null) {
            throw new IllegalArgumentException("The message is mandatory for the status INVITED_TO_INTERVIEW");
        }

        if (ALLOWED_TRANSITIONS.getOrDefault(currentStatus, List.of()).contains(newStatus)) {
            vacancyApplication.setApplicationStatus(newStatus);
            applicationRepository.save(vacancyApplication);

            Applicant applicant = vacancyApplication.getApplicant();
            Employer employer = vacancyApplication.getVacancy().getEmployer();

            String notificationMessage = String.format(
                    "Вас пригласили на собеседование в %s. Подготовьтесь к встрече и ознакомьтесь с деталями.",
                    employer.getName()
            );
            notificationService.createNotification(
                    applicant,
                    null,
                    notificationMessage,
                    NotificationType.INTERVIEW
            );

            ChatRoom chatRoom = chatRoomService.getOrCreateChat(applicant, employer);
            chatRoomService.sendMessage(chatRoom.getId(), new MessageRequest(
                            employer.getUser().getId(),
                            applicant.getUser().getId(),
                            request.message(),
                            MessageType.TEXT
                    )

            );

            chatRoomService.sendMessage(chatRoom.getId(), new MessageRequest(
                            employer.getUser().getId(),
                            applicant.getUser().getId(),
                            request.agreementUrl(),
                            MessageType.FILE
                    )
            );

            log.info("Status updated successfully for applicationId: {}", applicationId);
            return "Status changed successfully";
        } else {
            throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    @Override
    public List<PositionResponse> getPositionByApplicant(UUID applicantId) {
        Employer currentEmployer = userContext.getCurrentUser().getEmployer();
        List<VacancyApplication> vacancyApplications = applicationRepository.findAllByApplicant_IdAndVacancy_Employer_Id(
                applicantId, currentEmployer.getId());


        return vacancyApplications.stream()
                .map(v -> {
                            Vacancy vacancy = v.getVacancy();
                            return new PositionResponse(
                                    v.getId(),
                                    vacancy.isOtherPositionSelected()
                                            ? vacancy.getOtherPosition()
                                            : Objects.requireNonNull(vacancy.getPosition()).toString(),
                                    v.getApplicationStatus());
                        }
                ).toList();
    }

    private static final Map<ApplicationStatus, List<ApplicationStatus>> ALLOWED_TRANSITIONS = Map.of(
            ApplicationStatus.SENT, List.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.REJECTED),
            ApplicationStatus.UNDER_REVIEW, List.of(ApplicationStatus.INVITED_TO_INTERVIEW, ApplicationStatus.REJECTED),
            ApplicationStatus.INVITED_TO_INTERVIEW, List.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED)
    );

    private Specification<VacancyApplication> buildSpecificationApplication(
            String keyword,
            ApplicationStatus status,
            String dateFilter,
            UUID currentApplicantId) {

        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("applicant").get("id"), currentApplicantId));
            predicates.add(cb.equal(root.get("vacancy").get("status"), Status.ACTIVE));

            if (StringUtils.hasText(keyword)) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("vacancy").get("employer").get("name")), searchPattern),
                        cb.like(cb.lower(root.get("vacancy").get("position")), searchPattern),
                        cb.like(cb.lower(root.get("vacancy").get("otherPosition")), searchPattern),
                        cb.like(cb.lower(root.get("vacancy").get("industry")), searchPattern)
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("application_status"), status));
            }

            if (StringUtils.hasText(dateFilter)) {
                LocalDate fromDate = switch (dateFilter.toLowerCase()) {
                    case "last_day" -> LocalDate.now().minusDays(1);
                    case "last_week" -> LocalDate.now().minusWeeks(1);
                    case "last_month" -> LocalDate.now().minusMonths(1);
                    default -> null;
                };

                if (fromDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    private ApplicantApplicationsResponse toResponse(VacancyApplication vacancyApplication) {
        Vacancy vacancy = vacancyApplication.getVacancy();
        Employer employer = vacancy.getEmployer();
        return new ApplicantApplicationsResponse(
                employer.getId(),
                vacancy.getId(),
                employer.getLogoUrl(),
                employer.getName(),
                vacancy.isOtherPositionSelected()
                        ? vacancy.getOtherPosition()
                        : Objects.requireNonNull(vacancy.getPosition()).toString(),
                vacancy.getIndustry(),
                vacancyApplication.getCreatedAt(),
                vacancyApplication.getApplicationStatus()
        );
    }

    private Specification<VacancyApplication> buildSpecificationEmployer(
            UUID employerId,
            String keyword,
            String experienceString,
            ApplicationStatus status,
            String dateFilter) {

        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("vacancy").get("employer").get("id"), employerId));

            if (StringUtils.hasText(keyword)) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("applicant").get("firstName")), searchPattern),
                        cb.like(cb.lower(root.get("applicant").get("lastName")), searchPattern),
                        cb.like(cb.lower(root.get("vacancy").get("position")), searchPattern),
                        cb.like(cb.lower(root.get("vacancy").get("otherPosition")), searchPattern),
                        cb.like(cb.lower(root.get("vacancy").get("industry")), searchPattern)
                ));
            }

            if (StringUtils.hasText(experienceString)) {
                Experience experience = Experience.valueOf(experienceString.toUpperCase());
                predicates.add(cb.equal(root.get("applicant").get("profSkills").get("experience"), experience));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("application_status"), status));
            }

            if (StringUtils.hasText(dateFilter)) {
                LocalDate fromDate = switch (dateFilter.toLowerCase()) {
                    case "last_day" -> LocalDate.now().minusDays(1);
                    case "last_week" -> LocalDate.now().minusWeeks(1);
                    case "last_month" -> LocalDate.now().minusMonths(1);
                    default -> null;
                };

                if (fromDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                            root.get("createdAt").as(LocalDateTime.class),
                            fromDate.atStartOfDay()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    private VacancyCandidatesResponse mapToVacancyCandidateResponse(VacancyApplication vacancyApplication) {
        Applicant applicant = vacancyApplication.getApplicant();
        Vacancy vacancy = vacancyApplication.getVacancy();

        log.debug("Calculating total experience for applicant: {}", applicant.getFirstName() + " " + applicant.getLastName());

        return new VacancyCandidatesResponse(
                applicant.getId(),
                applicant.getFirstName(),
                applicant.getLastName(),
                vacancy.isOtherPositionSelected()
                        ? vacancy.getOtherPosition()
                        : Objects.requireNonNull(vacancy.getPosition()).toString(),
                vacancy.getIndustry(),
                applicant.getProfSkills().getExperience(),
                applicant.getCountry(),
                applicant.getCity(),
                vacancyApplication.getCreatedAt(),
                vacancyApplication.getId(),
                vacancyApplication.getApplicationStatus()
        );
    }

    private Applicant getCurrentApplicant() {
        return applicantRepository.findApplicantByUserEmail(jwtTokenUtil.getCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("Current applicant not found"));
    }
}
