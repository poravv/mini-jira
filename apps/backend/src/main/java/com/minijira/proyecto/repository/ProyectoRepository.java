package com.minijira.proyecto.repository;

import com.minijira.proyecto.entity.Proyecto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    /** Listado del módulo: trae los miembros en la misma consulta para no disparar un select por proyecto. */
    @EntityGraph(attributePaths = "members")
    List<Proyecto> findAllByOrderByNameAsc();
}
