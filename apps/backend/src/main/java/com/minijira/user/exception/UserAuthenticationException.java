package com.minijira.user.exception;

public class UserAuthenticationException extends RuntimeException {

    public UserAuthenticationException() {
        super("Invalid credentials");
    }
}
