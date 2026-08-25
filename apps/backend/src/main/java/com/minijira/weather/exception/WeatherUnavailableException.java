package com.minijira.weather.exception;

/** Se lanza cuando no se pudo obtener el clima desde Open-Meteo; GlobalExceptionHandler la convierte en un 503. */
public class WeatherUnavailableException extends RuntimeException {

    public WeatherUnavailableException(String message) {
        super(message);
    }

    public WeatherUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
