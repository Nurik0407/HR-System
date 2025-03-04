package com.example.border.service.impl;

import com.example.border.config.jwt.JwtTokenUtil;
import com.example.border.exception.ApplicationAlreadySubmittedException;
import com.example.border.exception.NotFoundException;
import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.dto.vacancyApplication.ApplicantApplicationsResponse;
import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;
import com.example.border.model.entity.*;
import com.example.border.model.enums.ApplicationStatus;
import com.example.border.model.enums.MessageType;
import com.example.border.model.enums.NotificationType;
import com.example.border.model.enums.Status;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
                        MessageType.CV
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

        Applicant currentApplicant = userContext.getCurrentUser().getApplicant();
        log.info("Fetching applications for applicant: {}, page: {}, size: {}, keyword: {}, status: {}, dateFilter: {}",
                currentApplicant.getId(), page, size, keyword, status, dateFilter);

        Pageable pageable = PageRequest.of(page, size);

        Specification<VacancyApplication> specification = buildSpecificationApplication(
                keyword, status, dateFilter, currentApplicant.getId()
        );

        Page<VacancyApplication> vacancyApplications = applicationRepository.findAll(specification, pageable);

        log.info("Fetched {} applications for user: {}", vacancyApplications.getTotalElements(), currentApplicant.getId());

        return vacancyApplications.map(this::toResponse);
    }

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

    private Applicant getCurrentApplicant() {
        return applicantRepository.findApplicantByUserEmail(jwtTokenUtil.getCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("Current applicant not found"));
    }
}
