package com.minijira.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Forma del JSON que devuelve Open-Meteo. Solo se mapea el bloque "current" y los campos que usamos;
 * el resto se ignora para que un cambio en la API externa no rompa la deserialización.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Current current) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            @JsonProperty("temperature_2m") Double temperature,
            @JsonProperty("relative_humidity_2m") Integer humidity,
            @JsonProperty("weather_code") Integer weatherCode,
            @JsonProperty("wind_speed_10m") Double windSpeed
    ) {
    }
}
