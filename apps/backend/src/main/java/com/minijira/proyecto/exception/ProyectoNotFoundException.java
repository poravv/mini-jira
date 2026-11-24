package com.minijira.proyecto.exception;

public class ProyectoNotFoundException extends RuntimeException {

    public ProyectoNotFoundException(Long id) {
        super("Project not found: " + id);
    }
}
