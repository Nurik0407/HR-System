package com.example.border.exception.handler;

import com.example.border.exception.*;
import com.example.border.model.dto.exception.ExceptionResponse;
import io.minio.errors.MinioException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.FileNotFoundException;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleValidException(MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();

        return new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                e.getClass().getSimpleName(),
                errorMessages.toString()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleBadCredentialsException(BadCredentialsException e) {
        return new ExceptionResponse(
                HttpStatus.FORBIDDEN,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNotFoundException(NotFoundException e) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return new ExceptionResponse(
                HttpStatus.CONFLICT,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(UserAlreadyEnabledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleUserAlreadyEnabledException(UserAlreadyEnabledException e) {
        return new ExceptionResponse(
                HttpStatus.CONFLICT,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleVerificationCodeExpiredException(VerificationCodeExpiredException e) {
        return new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleIllegalArgumentException(IllegalArgumentException e) {
        return new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleInvalidRoleException(InvalidRoleException e) {
        return new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleUnauthorizedAccessException(UnauthorizedAccessException e) {
        return new ExceptionResponse(
                HttpStatus.FORBIDDEN,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(ApplicationAlreadySubmittedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleApplicationAlreadySubmittedException(ApplicationAlreadySubmittedException e) {
        return new ExceptionResponse(
                HttpStatus.CONFLICT,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(MinioException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleMinioException(MinioException e) {
        return new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ExceptionResponse handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return new ExceptionResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                exc.getClass().getSimpleName(),
                "Файл слишком большой. Пожалуйста, загрузите файл меньшего размера."
        );
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleFileNotFoundException(FileNotFoundException exc) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND,
                exc.getClass().getSimpleName(),
                exc.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleIllegalStateException(IllegalStateException e) {
        return new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }
}
