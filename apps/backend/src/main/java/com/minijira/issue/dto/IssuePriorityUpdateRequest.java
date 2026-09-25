package com.minijira.issue.dto;

import com.minijira.issue.entity.IssuePriority;
import jakarta.validation.constraints.NotNull;

/** Payload para cambiar únicamente la prioridad de una incidencia. */
public record IssuePriorityUpdateRequest(@NotNull IssuePriority priority) {
}
