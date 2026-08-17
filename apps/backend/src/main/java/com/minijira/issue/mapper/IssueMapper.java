package com.minijira.issue.mapper;

import com.minijira.issue.dto.IssueRequest;
import com.minijira.issue.dto.IssueResponse;
import com.minijira.issue.entity.Issue;

/** Convierte entre los DTO de la API y la entidad JPA, para que el resto del código no mezcle ambas capas. */
public final class IssueMapper {

    private IssueMapper() {
    }

    public static Issue toEntity(IssueRequest request) {
        Issue issue = new Issue();
        issue.setTitle(request.title());
        issue.setDescription(request.description());
        // status/priority son opcionales: si vienen null se conservan los defaults de la entidad (PENDIENTE / MEDIA).
        if (request.status() != null) {
            issue.setStatus(request.status());
        }
        if (request.priority() != null) {
            issue.setPriority(request.priority());
        }
        return issue;
    }

    public static IssueResponse toResponse(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );
    }

    public static void updateEntity(Issue issue, IssueRequest request) {
        issue.setTitle(request.title());
        issue.setDescription(request.description());
        if (request.status() != null) {
            issue.setStatus(request.status());
        }
        if (request.priority() != null) {
            issue.setPriority(request.priority());
        }
    }
}
