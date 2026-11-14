package com.minijira.issue.exception;

public class IssueStatusTransitionException extends RuntimeException {

    public IssueStatusTransitionException(String message) {
        super(message);
    }
}