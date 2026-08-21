package com.minijira.issue.repository;

import com.minijira.issue.entity.Issue;
import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Acceso a datos de incidencias: Spring Data JPA genera la implementación (findAll, findById, save...) en runtime. */
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Busca incidencias filtrando por estado y/o prioridad. Cada filtro es opcional:
     * si llega null, esa condición no se aplica y no recorta el resultado.
     */
    @Query("""
            select i from Issue i
            where (:status is null or i.status = :status)
              and (:priority is null or i.priority = :priority)
            """)
    List<Issue> search(@Param("status") IssueStatus status, @Param("priority") IssuePriority priority);
}
