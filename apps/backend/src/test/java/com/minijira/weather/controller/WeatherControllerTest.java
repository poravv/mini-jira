package com.minijira.weather.controller;

import com.minijira.weather.dto.OpenMeteoResponse;
import com.minijira.weather.repository.WeatherRepository;
import com.minijira.weather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba controller + service + handler global juntos; solo se mockea el acceso al proveedor externo. */
@WebMvcTest(WeatherController.class)
@Import(WeatherService.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherRepository weatherRepository;

    @Test
    void should_return_current_weather_when_open_meteo_responds() throws Exception {
        given(weatherRepository.fetchCurrentWeather())
                .willReturn(new OpenMeteoResponse(new OpenMeteoResponse.Current(24.1, 60, 3, 12.3)));

        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Asunción"))
                .andExpect(jsonPath("$.temperature").value(24.1))
                .andExpect(jsonPath("$.humidity").value(60))
                .andExpect(jsonPath("$.weatherCode").value(3))
                .andExpect(jsonPath("$.windSpeed").value(12.3));
    }

    @Test
    void should_return_503_when_open_meteo_fails() throws Exception {
        given(weatherRepository.fetchCurrentWeather())
                .willThrow(new ResourceAccessException("Read timed out"));

        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Weather service unavailable"));
    }

    @Test
    void should_return_503_when_open_meteo_response_has_no_current_block() throws Exception {
        given(weatherRepository.fetchCurrentWeather()).willReturn(new OpenMeteoResponse(null));

        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").exists());
    }
}
