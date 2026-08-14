package com.minijira.issue.exception;

/** Se lanza cuando un id de incidencia no existe; GlobalExceptionHandler la convierte en un 404. */
public class IssueNotFoundException extends RuntimeException {

    public IssueNotFoundException(Long id) {
        super("Issue not found: " + id);
    }
}
