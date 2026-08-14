package com.minijira.issue.service;

import com.minijira.issue.dto.IssueRequest;
import com.minijira.issue.dto.IssueResponse;
import com.minijira.issue.entity.Issue;
import com.minijira.issue.exception.IssueNotFoundException;
import com.minijira.issue.mapper.IssueMapper;
import com.minijira.issue.repository.IssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio de incidencias: orquesta repository + mapper y decide qué es un error
 * (id inexistente → IssueNotFoundException). No conoce nada de HTTP.
 */
@Service
@Transactional
public class IssueService {

    private static final Logger log = LoggerFactory.getLogger(IssueService.class);

    private final IssueRepository issueRepository;

    public IssueService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> findAll() {
        return issueRepository.findAll().stream()
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

    private Issue getIssue(Long id) {
        return issueRepository.findById(id).orElseThrow(() -> {
            log.warn("Issue not found: id={}", id);
            return new IssueNotFoundException(id);
        });
    }
}
