package com.minijira.common;

import com.minijira.issue.exception.IssueNotFoundException;
import com.minijira.weather.exception.WeatherUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce excepciones de cualquier controller a respuestas JSON consistentes:
 * IssueNotFoundException → 404, WeatherUnavailableException → 503 y errores de validación de @Valid → 400 con detalle por campo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IssueNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(IssueNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(WeatherUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleWeatherUnavailable(WeatherUnavailableException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ? "invalid" : fieldError.getDefaultMessage(),
                        (first, second) -> first
                ));
        return Map.of("error", "Validation failed", "fields", fieldErrors);
    }
}
