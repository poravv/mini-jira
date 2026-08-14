package com.minijira.issue.dto;

import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;

import java.time.Instant;

/** DTO de salida: la representación JSON de una incidencia que devuelve la API (nunca se expone la entidad JPA). */
public record IssueResponse(
        Long id,
        String title,
        String description,
        IssueStatus status,
        IssuePriority priority,
        Instant createdAt,
        Instant updatedAt
) {
}
