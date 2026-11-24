import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Weather, describeWeatherCode } from './weather.model';
import { WeatherService } from './weather.service';

describe('WeatherService', () => {
  let service: WeatherService;
  let httpTesting: HttpTestingController;

  const weather: Weather = {
    city: 'Asunción',
    temperature: 24.1,
    humidity: 60,
    weatherCode: 3,
    windSpeed: 12.3
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(WeatherService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('should_get_current_weather_from_api_weather', () => {
    service.getCurrent().subscribe((result) => expect(result).toEqual(weather));

    const request = httpTesting.expectOne('/api/weather');
    expect(request.request.method).toBe('GET');
    request.flush(weather);
  });
});

describe('describeWeatherCode', () => {
  it('should_map_known_wmo_codes_to_spanish_labels', () => {
    expect(describeWeatherCode(0)).toBe('Despejado');
    expect(describeWeatherCode(2)).toBe('Parcialmente nublado');
    expect(describeWeatherCode(45)).toBe('Niebla');
    expect(describeWeatherCode(61)).toBe('Lluvia o llovizna');
    expect(describeWeatherCode(75)).toBe('Nieve');
    expect(describeWeatherCode(81)).toBe('Chubascos');
    expect(describeWeatherCode(95)).toBe('Tormenta');
  });

  it('should_return_desconocido_when_code_is_not_in_table', () => {
    expect(describeWeatherCode(30)).toBe('Desconocido');
  });
});
