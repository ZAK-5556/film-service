package com.example.filmservice.controller;

import com.example.filmservice.dto.ErrorDto;
import com.example.filmservice.exception.BadRequestException;
import com.example.filmservice.exception.ConflictException;
import com.example.filmservice.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Converts exceptions into HTTP responses with an {@link ErrorDto} body. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto handleNotFound(NotFoundException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto handleBadRequest(BadRequestException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDto handleConflict(ConflictException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDto handleDataIntegrity(DataIntegrityViolationException e) {
        return new ErrorDto("Data integrity violation");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return new ErrorDto("Validation failed", fieldErrors);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, JsonProcessingException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto handleUnreadable(Exception e) {
        return new ErrorDto("Malformed JSON");
    }
}
