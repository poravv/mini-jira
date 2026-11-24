package com.minijira.proyecto.exception;

public class ProyectoMemberConflictException extends RuntimeException {

    public ProyectoMemberConflictException(Long projectId, Long userId) {
        super("User " + userId + " is already a member of project " + projectId);
    }
}
