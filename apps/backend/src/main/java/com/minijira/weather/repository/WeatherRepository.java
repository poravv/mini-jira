package com.minijira.weather.repository;

import com.minijira.weather.dto.OpenMeteoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Fuente de datos del módulo weather. A diferencia de los otros repositorios,
 * los datos no salen de la base
 * sino del servicio externo Open-Meteo vía HTTP; el rol en la arquitectura es
 * el mismo (capa de acceso a datos).
 * La URL base viene de `weather.open-meteo.url` y los timeouts de
 * `spring.http.client.*` en application.yml.
 */
@Repository
public class WeatherRepository {

    /**
     * Coordenadas fijas de Asunción: el endpoint solo informa esta ciudad, el
     * cliente no elige ubicación.
     */
    private static final double ASUNCION_LATITUDE = -25.2637;
    private static final double ASUNCION_LONGITUDE = -57.5759;
    private static final String CURRENT_FIELDS = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m";

    private final RestClient restClient;

    public WeatherRepository(RestClient.Builder restClientBuilder,
            @Value("${weather.open-meteo.url}") String openMeteoUrl) {
        this.restClient = restClientBuilder.baseUrl(openMeteoUrl).build();
    }

    /**
     * Pide a Open-Meteo el clima actual de Asunción.
     *
     * @return la respuesta cruda del proveedor (puede venir sin bloque "current" si
     *         el proveedor responde vacío).
     * @throws RestClientException si hay timeout, error de red, respuesta 4xx/5xx o
     *                             JSON inválido.
     */
    public OpenMeteoResponse fetchCurrentWeather() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", ASUNCION_LATITUDE)
                        .queryParam("longitude", ASUNCION_LONGITUDE)
                        .queryParam("current", CURRENT_FIELDS)
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);
    }
}
