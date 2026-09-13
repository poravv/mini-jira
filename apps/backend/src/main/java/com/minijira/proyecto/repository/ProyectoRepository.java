package com.minijira.proyecto.repository;

import com.minijira.proyecto.entity.Proyecto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    default List<Proyecto> findAllOrdered() {
        return findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
