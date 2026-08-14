package com.minijira.issue.dto;

import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada (body del POST): lo que el cliente puede enviar al crear una incidencia.
 * status y priority son opcionales; si vienen null la entidad usa sus defaults.
 */
public record IssueRequest(
        @NotBlank @Size(max = 150) String title,
        String description,
        IssueStatus status,
        IssuePriority priority
) {
}
