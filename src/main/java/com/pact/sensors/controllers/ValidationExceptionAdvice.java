package com.pact.sensors.controllers;

import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ValidationExceptionAdvice {
    final static String ERROR_FORMAT = "'%s' parameter %s";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleMNVE(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getAllErrors().stream()
                .map(error ->String.format(ERROR_FORMAT,((FieldError) error).getField(), error.getDefaultMessage()))
                .sorted() //Make error messages' order consistent
                .collect(Collectors.toList());

        return getResponseEntity(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Problem> handleCVE(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
                .map(error -> String.format(ERROR_FORMAT, error.getPropertyPath(), error.getMessage()))
                .collect(Collectors.toList());

        return getResponseEntity(errors);
    }

    ResponseEntity<Problem> getResponseEntity(List<String> errors) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Problem.create()
                        .withTitle("Invalid data has been provided, please correct it")
                        .withDetail(errors.toString()));
    }
}
