package com.example.border.exception;

public class UserAlreadyEnabledException extends RuntimeException{
    public UserAlreadyEnabledException(String message) {
        super(message);
    }
}
