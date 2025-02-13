package com.example.border.exception;

public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException(String message) {
        super(message);
    }

    public InvalidRoleException(String message, IllegalArgumentException ex) {
        super(message);
    }
}
