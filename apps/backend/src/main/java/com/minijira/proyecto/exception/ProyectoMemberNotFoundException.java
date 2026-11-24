package com.minijira.proyecto.exception;

public class ProyectoMemberNotFoundException extends RuntimeException {

    public ProyectoMemberNotFoundException(Long projectId, Long userId) {
        super("User " + userId + " is not a member of project " + projectId);
    }
}
