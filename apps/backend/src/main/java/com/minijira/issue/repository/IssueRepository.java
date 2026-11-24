package com.minijira.issue.repository;

import com.minijira.issue.entity.Issue;
import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Acceso a datos de incidencias: Spring Data JPA genera la implementación (findAll, findById, save...) en runtime. */
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Busca incidencias filtrando por estado, prioridad, proyecto y asignado. Cada filtro es opcional:
     * si llega null, esa condición no se aplica y no recorta el resultado.
     */
    @EntityGraph(attributePaths = {"proyecto", "assignee"})
    @Query("""
            select i from Issue i
            where (:status is null or i.status = :status)
              and (:priority is null or i.priority = :priority)
              and (:projectId is null or i.proyecto.id = :projectId)
              and (:assigneeId is null or i.assignee.id = :assigneeId)
            """)
    List<Issue> search(@Param("status") IssueStatus status,
                       @Param("priority") IssuePriority priority,
                       @Param("projectId") Long projectId,
                       @Param("assigneeId") Long assigneeId);

    /** Desasigna las incidencias de un proyecto que estaban a cargo de un usuario. Devuelve cuántas cambiaron. */
    @Modifying(flushAutomatically = true)
    @Query("update Issue i set i.assignee = null where i.proyecto.id = :projectId and i.assignee.id = :assigneeId")
    int clearAssignee(@Param("projectId") Long projectId, @Param("assigneeId") Long assigneeId);

    /** Deja sin proyecto ni asignado a todas las incidencias de un proyecto. Devuelve cuántas cambiaron. */
    @Modifying(flushAutomatically = true)
    @Query("update Issue i set i.proyecto = null, i.assignee = null where i.proyecto.id = :projectId")
    int clearProjectAndAssignee(@Param("projectId") Long projectId);
}
