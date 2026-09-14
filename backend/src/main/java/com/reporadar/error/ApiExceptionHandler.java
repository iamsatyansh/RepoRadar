package com.reporadar.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApiException(ApiException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        detail.setType(URI.create("https://reporadar.dev/problems/" + exception.getCode().toLowerCase()));
        detail.setProperty("code", exception.getCode());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleInvalidRequest(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("The request is invalid.");
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        detail.setProperty("code", "VALIDATION_FAILED");
        return detail;
    }
}
