package com.minijira.issue.exception;

/** Se lanza cuando una regla de workflow impide cambiar estado o prioridad. */
public class InvalidIssueTransitionException extends RuntimeException {

    public InvalidIssueTransitionException(String field, Object currentValue, Object requestedValue) {
        super("Invalid " + field + " transition: " + currentValue + " -> " + requestedValue);
    }
}
