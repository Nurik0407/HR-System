package com.example.border.service.impl;

import com.example.border.config.jwt.JwtTokenUtil;
import com.example.border.exception.ApplicationAlreadySubmittedException;
import com.example.border.exception.NotFoundException;
import com.example.border.model.dto.message.MessageRequest;
import com.example.border.model.dto.vacancyApplication.VacancyApplicationRequest;
import com.example.border.model.entity.*;
import com.example.border.model.enums.ApplicationStatus;
import com.example.border.model.enums.MessageType;
import com.example.border.model.enums.NotificationType;
import com.example.border.repository.ApplicantRepository;
import com.example.border.repository.VacancyApplicationRepository;
import com.example.border.repository.VacancyRepository;
import com.example.border.service.ChatMessageService;
import com.example.border.service.ChatService;
import com.example.border.service.NotificationService;
import com.example.border.service.VacancyApplicationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

    private final VacancyApplicationRepository applicationRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final VacancyRepository vacancyRepository;
    private final ApplicantRepository applicantRepository;
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;
    private final ChatService chatService;
    private final ChatMessageService chatMessageService;

    public VacancyApplicationServiceImpl(VacancyApplicationRepository applicationRepository, JwtTokenUtil jwtTokenUtil, VacancyRepository vacancyRepository, ApplicantRepository applicantRepository, NotificationService notificationService, WebSocketService webSocketService, ChatService chatService, ChatMessageService chatMessageService) {
        this.applicationRepository = applicationRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.vacancyRepository = vacancyRepository;
        this.applicantRepository = applicantRepository;
        this.notificationService = notificationService;
        this.webSocketService = webSocketService;
        this.chatService = chatService;
        this.chatMessageService = chatMessageService;
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

        ChatRoom chatRoom = chatService.getOrCreateChat(currentApplicant, employer);

        chatService.sendMessage(chatRoom.getId(),
                new MessageRequest(
                        currentApplicant.getUser().getId(),
                        employer.getUser().getId(),
                        request.coverLetter(),
                        MessageType.COVER_LETTER
                ));

        chatService.sendMessage(chatRoom.getId(),
                new MessageRequest(
                        currentApplicant.getUser().getId(),
                        employer.getUser().getId(),
                        request.CVUrl(),
                        MessageType.CV
                ));

        return "The application is successfully sent";
    }

    private Applicant getCurrentApplicant() {
        return applicantRepository.findApplicantByUserEmail(jwtTokenUtil.getCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("Current applicant not found"));
    }
}
