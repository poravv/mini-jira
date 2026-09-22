package com.minijira.proyecto.controller;

import com.minijira.proyecto.dto.MemberRequest;
import com.minijira.proyecto.dto.ProyectoRequest;
import com.minijira.proyecto.dto.ProyectoResponse;
import com.minijira.proyecto.service.ProyectoService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@Tag(name = "Projects", description = "Project CRUD and membership management")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    @GetMapping
    @Operation(summary = "List projects")
    public List<ProyectoResponse> list() {
        return proyectoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one project by id")
    public ProyectoResponse get(@PathVariable Long id) {
        return proyectoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a project")
    public ProyectoResponse create(@Valid @RequestBody ProyectoRequest request) {
        return proyectoService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project")
    public ProyectoResponse update(@PathVariable Long id, @Valid @RequestBody ProyectoRequest request) {
        return proyectoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a project")
    public void delete(@PathVariable Long id) {
        proyectoService.deleteById(id);
    }

    @PostMapping("/{id}/miembros")
    @Operation(summary = "Add an existing user to a project")
    public ProyectoResponse addMember(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return proyectoService.addMember(id, request.userId());
    }

    @DeleteMapping("/{id}/miembros/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a user from a project")
    public void removeMember(@PathVariable Long id, @PathVariable Long userId) {
        proyectoService.removeMember(id, userId);
    }
}
