package com.minijira.issue.entity;

/** Estado del ciclo de vida de una incidencia; se guarda como texto en la columna "status" (default PENDIENTE). */
public enum IssueStatus {
    PENDIENTE, EN_PROGRESO, RESUELTA, CERRADA
}
