package com.minijira.weather.controller;

import com.minijira.weather.dto.WeatherDto;
import com.minijira.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST del clima bajo /api/weather. Solo delega en WeatherService;
 * si el proveedor externo falla, GlobalExceptionHandler responde 503.
 */
@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather", description = "Clima actual de Asunción (proxy de Open-Meteo)")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    @Operation(summary = "Get current weather",
            description = "Devuelve el clima actual de Asunción. Responde 503 si el proveedor externo no está disponible.")
    public WeatherDto getCurrentWeather() {
        return weatherService.getCurrentWeather();
    }
}
