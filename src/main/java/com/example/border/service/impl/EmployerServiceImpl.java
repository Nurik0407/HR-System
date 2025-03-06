package com.example.border.service.impl;

import com.example.border.model.dto.employer.EmployerDto;
import com.example.border.model.entity.Employer;
import com.example.border.repository.EmployerRepository;
import com.example.border.service.EmployerService;
import com.example.border.utils.UserContext;
import io.jsonwebtoken.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class EmployerServiceImpl implements EmployerService {

    private static final Logger log = LoggerFactory.getLogger(EmployerServiceImpl.class);
    private final UserContext userContext;
    private final EmployerRepository employerRepository;

    public EmployerServiceImpl(UserContext userContext, EmployerRepository employerRepository) {
        this.userContext = userContext;
        this.employerRepository = employerRepository;
    }

    @Override
    public EmployerDto getProfile() {
        log.debug("Fetching employer profile");
        Employer employer = getCurrentEmployer();
        log.debug("Successfully fetched profile for employer ID: {}", employer.getId());
        return mapToEmployerDto(employer);
    }

    @Override
    public EmployerDto updateProfile(EmployerDto employerDto) {
        log.info("Starting profile update");
        Employer employer = getCurrentEmployer();

        updateField(employerDto.name(), employer::getName, employer::setName, "name", employer);
        updateField(employerDto.logoUrl(), employer::getLogoUrl, employer::setLogoUrl, "logoUrl", employer);
        updateField(employerDto.aboutCompany(), employer::getAboutCompany, employer::setAboutCompany, "aboutCompany", employer);
        updateField(employerDto.country(), employer::getCountry, employer::setCountry, "country", employer);
        updateField(employerDto.city(), employer::getCity, employer::setCity, "city", employer);
        updateField(employerDto.address(), employer::getAddress, employer::setAddress, "address", employer);
        updateField(employerDto.phoneNumber(), employer::getPhoneNumber, employer::setPhoneNumber, "phoneNumber", employer);

        Employer updatedEmployer = employerRepository.save(employer);
        log.info("Profile updated successfully for employer ID: {}", updatedEmployer.getId());

        return mapToEmployerDto(updatedEmployer);
    }

    private <T> void updateField(
            T newValue,
            Supplier<T> currentValueGetter,
            Consumer<T> setter,
            String fieldName,
            Employer employer) {
        if (newValue != null && !newValue.equals(currentValueGetter.get())) {
            setter.accept(newValue);
            log.debug("Updated field '{}' for employer ID: {}. New value: {}",
                    fieldName, employer.getId(), newValue);
        }
    }

    private EmployerDto mapToEmployerDto(Employer employer) {
        return new EmployerDto(
                employer.getName(),
                employer.getLogoUrl(),
                employer.getAboutCompany(),
                employer.getCountry(),
                employer.getCity(),
                employer.getAddress(),
                employer.getUser().getEmail(),
                employer.getPhoneNumber()
        );
    }

    private Employer getCurrentEmployer() {
        Employer employer = userContext.getCurrentUser().getEmployer();
        Assert.notNull(employer, "Employer entity not found for current user");
        return employer;
    }
}
