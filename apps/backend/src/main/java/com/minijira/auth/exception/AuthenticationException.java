package com.minijira.auth.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
        super("Invalid credentials or inactive account");
    }
}
