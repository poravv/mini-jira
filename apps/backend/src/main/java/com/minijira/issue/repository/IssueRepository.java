package com.minijira.issue.repository;

import com.minijira.issue.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de incidencias: Spring Data JPA genera la implementación (findAll, findById, save...) en runtime. */
public interface IssueRepository extends JpaRepository<Issue, Long> {
}
