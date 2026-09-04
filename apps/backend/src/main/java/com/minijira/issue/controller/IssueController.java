package com.minijira.issue.controller;

import com.minijira.issue.dto.IssueRequest;
import com.minijira.issue.dto.IssueResponse;
import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import com.minijira.issue.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Endpoints REST de incidencias bajo /api/issues. Solo traduce HTTP ↔ DTO y
 * delega toda la lógica en IssueService; los errores los convierte GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issues", description = "Issue tracking CRUD")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    @Operation(summary = "List issues",
            description = "Filtra por estado y prioridad (ambos opcionales) y devuelve las incidencias más urgentes primero.")
    public List<IssueResponse> list(@RequestParam(required = false) IssueStatus status,
                                    @RequestParam(required = false) IssuePriority priority) {
        return issueService.findAll(status, priority);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one issue by id")
    public IssueResponse get(@PathVariable Long id) {
        return issueService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an issue")
    public IssueResponse create(@Valid @RequestBody IssueRequest request) {
        return issueService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Update an issue")
    public IssueResponse update(@PathVariable Long id, @Valid @RequestBody IssueRequest request) {
        return issueService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an issue by id")
    public void delete(@PathVariable Long id) {
        issueService.deleteById(id);
    }

}
