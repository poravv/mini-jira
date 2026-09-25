package com.minijira.issue.dto;

import com.minijira.issue.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;

/** Payload para cambiar únicamente el estado de una incidencia. */
public record IssueStatusUpdateRequest(@NotNull IssueStatus status) {
}
