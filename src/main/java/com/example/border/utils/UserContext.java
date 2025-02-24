package com.example.border.utils;

import com.example.border.exception.NotFoundException;
import com.example.border.exception.UnauthorizedAccessException;
import com.example.border.model.entity.User;
import com.example.border.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContext {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserContext.class);

    public UserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new UnauthorizedAccessException("Пользователь не авторизован!");
            }

            String email = authentication.getName();

            if (email == null || email.isEmpty()) {
                throw new UnauthorizedAccessException("Имя пользователя не найдено!");
            }

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        } catch (UnauthorizedAccessException | NotFoundException e) {
            logger.error("Ошибка получения текущего пользователя: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении данных о пользователе: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при получении данных о пользователе.", e);
        }
    }
}
