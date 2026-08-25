package com.minijira.weather.service;

import com.minijira.weather.dto.OpenMeteoResponse;
import com.minijira.weather.dto.WeatherDto;
import com.minijira.weather.exception.WeatherUnavailableException;
import com.minijira.weather.repository.WeatherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Traduce la respuesta de Open-Meteo al contrato de la API. Es el único lugar que sabe que el clima viene
 * de un proveedor externo: cualquier fallo de ese proveedor se convierte en WeatherUnavailableException,
 * así el controller no conoce detalles de HTTP ni de Open-Meteo.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private static final String CITY = "Asunción";

    private final WeatherRepository weatherRepository;

    public WeatherService(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    /**
     * Devuelve el clima actual de Asunción.
     *
     * @throws WeatherUnavailableException si Open-Meteo no responde, responde con error o con un JSON sin datos.
     */
    public WeatherDto getCurrentWeather() {
        OpenMeteoResponse response;
        try {
            response = weatherRepository.fetchCurrentWeather();
        } catch (RestClientException ex) {
            log.error("[WEATHER] Open-Meteo no disponible: {}", ex.getMessage());
            throw new WeatherUnavailableException("Weather service unavailable", ex);
        }

        if (response == null || response.current() == null) {
            log.error("[WEATHER] Open-Meteo respondió sin el bloque 'current'");
            throw new WeatherUnavailableException("Weather service returned an invalid response");
        }

        OpenMeteoResponse.Current current = response.current();
        log.info("[WEATHER] Clima actual obtenido: temperature={} weatherCode={}", current.temperature(), current.weatherCode());
        return new WeatherDto(CITY, current.temperature(), current.humidity(), current.weatherCode(), current.windSpeed());
    }
}
