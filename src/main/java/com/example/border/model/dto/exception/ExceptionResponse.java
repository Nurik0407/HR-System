package com.example.border.model.dto.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;


public class ExceptionResponse {

    @JsonProperty
    private HttpStatus status;
    @JsonProperty
    private String exception;
    @JsonProperty
    private String details;

    public ExceptionResponse(HttpStatus httpStatus, String exceptionClassName, String message) {
        this.status = httpStatus;
        this.exception = exceptionClassName;
        this.details = message;
    }
}
