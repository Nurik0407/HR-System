package com.example.border.service.impl;

import com.example.border.model.entity.Applicant;
import com.example.border.model.entity.Employer;
import com.example.border.model.entity.Favorite;
import com.example.border.repository.FavoriteRepository;
import com.example.border.service.ApplicantService;
import com.example.border.service.FavoriteService;
import com.example.border.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteServiceImpl.class);
    private final FavoriteRepository favoriteRepository;
    private final UserContext userContext;
    private final ApplicantService applicantService;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository, UserContext userContext, ApplicantService applicantService) {
        this.favoriteRepository = favoriteRepository;
        this.userContext = userContext;
        this.applicantService = applicantService;
    }

    @Override
    public String addFavorite(UUID applicantId) {
        Employer currentEmployer = userContext.getCurrentUser().getEmployer();
        Applicant applicant = applicantService.findApplicantById(applicantId);

        log.info("Adding applicant {} to favorites for employer {}", applicantId, currentEmployer.getId());

        if (favoriteRepository.existsByEmployerAndApplicant(currentEmployer, applicant)) {
            log.warn("Applicant {} is already in favorites for employer {}", applicantId, currentEmployer.getId());
            throw new IllegalStateException("The applicant is already in favorites");
        }

        Favorite favorite = new Favorite(currentEmployer, applicant);
        favoriteRepository.save(favorite);

        log.info("Successfully added applicant {} to favorites for employer {}", applicantId, currentEmployer.getId());
        return "The applicant was successfully added to favorites";
    }


    @Override
    public String removeFavorite(UUID applicantId) {
        Employer currentEmployer = userContext.getCurrentUser().getEmployer();
        Applicant applicant = applicantService.findApplicantById(applicantId);

        log.info("Removing applicant {} from favorites for employer {}", applicantId, currentEmployer.getId());

        if (!favoriteRepository.existsByEmployerAndApplicant(currentEmployer, applicant)) {
            log.warn("Applicant {} is not in favorites for employer {}", applicantId, currentEmployer.getId());
            throw new IllegalStateException("The applicant is not in favorites");
        }

        favoriteRepository.deleteFavoriteByApplicantAndEmployer(applicant, currentEmployer);

        log.info("Successfully removed applicant {} from favorites for employer {}", applicantId, currentEmployer.getId());
        return "The applicant was successfully removed from favorites";
    }


    @Override
    public Boolean isApplicantInFavorites(UUID applicantId) {
        Employer currentEmployer = userContext.getCurrentUser().getEmployer();
        Applicant applicant = applicantService.findApplicantById(applicantId);

        boolean exists = favoriteRepository.existsByEmployerAndApplicant(currentEmployer, applicant);
        log.info("Checking if applicant {} is in favorites for employer {}: {}", applicantId, currentEmployer.getId(), exists);

        return exists;
    }
}
