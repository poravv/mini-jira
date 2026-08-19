package com.minijira.issue.service;

import com.minijira.issue.dto.IssueRequest;
import com.minijira.issue.dto.IssueResponse;
import com.minijira.issue.entity.Issue;
import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import com.minijira.issue.exception.IssueNotFoundException;
import com.minijira.issue.mapper.IssueMapper;
import com.minijira.issue.repository.IssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Reglas de negocio de incidencias: orquesta repository + mapper y decide qué es un error
 * (id inexistente → IssueNotFoundException). No conoce nada de HTTP.
 */
@Service
@Transactional
public class IssueService {

    private static final Logger log = LoggerFactory.getLogger(IssueService.class);

    /** Orden del listado: las más urgentes primero y, a igual prioridad, las más recientes antes. */
    private static final Comparator<Issue> MAS_URGENTES_PRIMERO =
            Comparator.comparing(Issue::getPriority)
                    .thenComparing(Issue::getCreatedAt, Comparator.reverseOrder());

    private final IssueRepository issueRepository;

    public IssueService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    /**
     * Lista incidencias aplicando los filtros que vengan informados (ambos son opcionales)
     * y las devuelve ordenadas por prioridad, de la más urgente a la menos urgente.
     */
    @Transactional(readOnly = true)
    public List<IssueResponse> findAll(IssueStatus status, IssuePriority priority) {
        return issueRepository.search(status, priority).stream()
                .sorted(MAS_URGENTES_PRIMERO)
                .map(IssueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IssueResponse findById(Long id) {
        return IssueMapper.toResponse(getIssue(id));
    }

    public IssueResponse create(IssueRequest request) {
        Issue issue = issueRepository.save(IssueMapper.toEntity(request));
        log.info("Issue created: id={} status={} priority={}", issue.getId(), issue.getStatus(), issue.getPriority());
        return IssueMapper.toResponse(issue);
    }

    public IssueResponse update(Long id, IssueRequest request) {
        Issue issue = getIssue(id);
        IssueMapper.updateEntity(issue, request);
        Issue updatedIssue = issueRepository.save(issue);
        log.info("Issue updated: id={} status={} priority={}", updatedIssue.getId(), updatedIssue.getStatus(), updatedIssue.getPriority());
        return IssueMapper.toResponse(updatedIssue);
    }

    public void deleteById(Long id) {
        Issue issue = getIssue(id);
        issueRepository.delete(issue);
        log.info("Issue deleted: id={}", id);
    }

    private Issue getIssue(Long id) {
        return issueRepository.findById(id).orElseThrow(() -> {
            log.warn("Issue not found: id={}", id);
            return new IssueNotFoundException(id);
        });
    }
}
