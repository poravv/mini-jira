package com.minijira.proyecto.mapper;

import com.minijira.proyecto.dto.ProyectoRequest;
import com.minijira.proyecto.dto.ProyectoResponse;
import com.minijira.proyecto.entity.Proyecto;
import com.minijira.user.mapper.UserMapper;

public final class ProyectoMapper {

    private ProyectoMapper() {
    }

    public static Proyecto toEntity(ProyectoRequest request) {
        Proyecto proyecto = new Proyecto();
        updateEntity(proyecto, request);
        return proyecto;
    }

    public static void updateEntity(Proyecto proyecto, ProyectoRequest request) {
        proyecto.setName(request.name().trim());
        proyecto.setDescription(normalizeDescription(request.description()));
    }

    public static ProyectoResponse toResponse(Proyecto proyecto) {
        return new ProyectoResponse(
                proyecto.getId(),
                proyecto.getName(),
                proyecto.getDescription(),
                proyecto.getCreatedAt(),
                proyecto.getUpdatedAt(),
                proyecto.getMembers().stream().map(UserMapper::toResponse).toList());
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
