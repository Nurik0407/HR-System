package com.example.border.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class InvalidRoleException extends OAuth2AuthenticationException {
    public InvalidRoleException(String message) {
        super(message);
    }

    public InvalidRoleException(String message, IllegalArgumentException ex) {
        super(message);
    }
}
