package com.minijira.weather.repository;

import com.minijira.weather.dto.OpenMeteoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Prueba el borde con Open-Meteo: la URL que se pide y cómo se deserializa su JSON (snake_case). */
@RestClientTest(WeatherRepository.class)
class WeatherRepositoryTest {

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void should_map_current_block_when_open_meteo_responds_ok() {
        server.expect(requestTo(startsWith("https://api.open-meteo.com/v1/forecast")))
                .andExpect(queryParam("latitude", "-25.2637"))
                .andExpect(queryParam("longitude", "-57.5759"))
                .andRespond(withSuccess("""
                        {"latitude": -25.25, "current": {"time": "2026-08-23T12:00", "temperature_2m": 24.1,
                         "relative_humidity_2m": 60, "weather_code": 3, "wind_speed_10m": 12.3}}
                        """, MediaType.APPLICATION_JSON));

        OpenMeteoResponse response = weatherRepository.fetchCurrentWeather();

        assertEquals(24.1, response.current().temperature());
        assertEquals(60, response.current().humidity());
        assertEquals(3, response.current().weatherCode());
        assertEquals(12.3, response.current().windSpeed());
    }

    @Test
    void should_throw_rest_client_exception_when_open_meteo_returns_5xx() {
        server.expect(requestTo(startsWith("https://api.open-meteo.com/v1/forecast")))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(RestClientException.class, weatherRepository::fetchCurrentWeather);
    }
}
