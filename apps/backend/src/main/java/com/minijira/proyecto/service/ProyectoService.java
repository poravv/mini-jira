package com.minijira.proyecto.service;

import com.minijira.issue.repository.IssueRepository;
import com.minijira.proyecto.dto.ProyectoRequest;
import com.minijira.proyecto.dto.ProyectoResponse;
import com.minijira.proyecto.entity.Proyecto;
import com.minijira.proyecto.exception.ProyectoMemberConflictException;
import com.minijira.proyecto.exception.ProyectoMemberNotFoundException;
import com.minijira.proyecto.exception.ProyectoNotFoundException;
import com.minijira.proyecto.mapper.ProyectoMapper;
import com.minijira.proyecto.repository.ProyectoRepository;
import com.minijira.user.entity.User;
import com.minijira.user.exception.UserNotFoundException;
import com.minijira.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProyectoService {

    private static final Logger log = LoggerFactory.getLogger(ProyectoService.class);

    private final ProyectoRepository proyectoRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;

    public ProyectoService(ProyectoRepository proyectoRepository,
                           UserRepository userRepository,
                           IssueRepository issueRepository) {
        this.proyectoRepository = proyectoRepository;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
    }

    @Transactional(readOnly = true)
    public List<ProyectoResponse> findAll() {
        return proyectoRepository.findAllByOrderByNameAsc().stream()
                .map(ProyectoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProyectoResponse findById(Long id) {
        return ProyectoMapper.toResponse(getProyecto(id));
    }

    public ProyectoResponse create(ProyectoRequest request) {
        Proyecto proyecto = proyectoRepository.save(ProyectoMapper.toEntity(request));
        log.info("Project created: id={}", proyecto.getId());
        return ProyectoMapper.toResponse(proyecto);
    }

    public ProyectoResponse update(Long id, ProyectoRequest request) {
        Proyecto proyecto = getProyecto(id);
        ProyectoMapper.updateEntity(proyecto, request);
        Proyecto updated = proyectoRepository.save(proyecto);
        log.info("Project updated: id={}", updated.getId());
        return ProyectoMapper.toResponse(updated);
    }

    /**
     * Borra el proyecto y deja sus incidencias sin proyecto ni asignado, en la misma transacción:
     * una incidencia con asignado pero sin proyecto es un estado que la asignación no permite crear.
     */
    public void deleteById(Long id) {
        Proyecto proyecto = getProyecto(id);
        int detachedIssues = issueRepository.clearProjectAndAssignee(id);
        proyectoRepository.delete(proyecto);
        log.info("Project deleted: id={} detachedIssues={}", id, detachedIssues);
    }

    public ProyectoResponse addMember(Long projectId, Long userId) {
        Proyecto proyecto = getProyecto(projectId);
        User user = getUser(userId);
        if (proyecto.getMembers().stream().anyMatch(member -> member.getId().equals(userId))) {
            throw new ProyectoMemberConflictException(projectId, userId);
        }

        proyecto.getMembers().add(user);
        Proyecto updated;
        try {
            updated = proyectoRepository.saveAndFlush(proyecto);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate project member rejected by the database: projectId={} userId={}", projectId, userId);
            throw new ProyectoMemberConflictException(projectId, userId);
        }
        log.info("Project member added: projectId={} userId={}", projectId, userId);
        return ProyectoMapper.toResponse(updated);
    }

    /**
     * Quita al usuario del proyecto y, en la misma transacción, desasigna las incidencias de ese
     * proyecto que tenía a cargo: si no, quedaría asignado a un proyecto del que ya no es miembro.
     */
    public void removeMember(Long projectId, Long userId) {
        Proyecto proyecto = getProyecto(projectId);
        getUser(userId);
        boolean removed = proyecto.getMembers().removeIf(member -> member.getId().equals(userId));
        if (!removed) {
            throw new ProyectoMemberNotFoundException(projectId, userId);
        }

        proyectoRepository.save(proyecto);
        int unassignedIssues = issueRepository.clearAssignee(projectId, userId);
        log.info("Project member removed: projectId={} userId={} unassignedIssues={}",
                projectId, userId, unassignedIssues);
    }

    private Proyecto getProyecto(Long id) {
        return proyectoRepository.findById(id).orElseThrow(() -> {
            log.warn("Project not found: id={}", id);
            return new ProyectoNotFoundException(id);
        });
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found while managing project membership: id={}", id);
            return new UserNotFoundException(id);
        });
    }
}
