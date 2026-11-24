package com.minijira.weather.dto;

/** DTO de salida: el clima actual tal como lo devuelve GET /api/weather (contrato acordado con el frontend). */
public record WeatherDto(
        String city,
        Double temperature,
        Integer humidity,
        Integer weatherCode,
        Double windSpeed
) {
}
