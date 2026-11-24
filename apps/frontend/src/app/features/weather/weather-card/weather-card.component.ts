import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';

import { Weather, describeWeatherCode } from '../weather.model';
import { WeatherService } from '../weather.service';

/** Tarjeta compacta con el clima actual: pide los datos a WeatherService y muestra carga, error u ok. */
@Component({
  selector: 'app-weather-card',
  imports: [DecimalPipe],
  templateUrl: './weather-card.component.html',
  styleUrl: './weather-card.component.css'
})
export class WeatherCardComponent implements OnInit {
  private readonly weatherService = inject(WeatherService);

  weather: Weather | null = null;
  isLoading = true;
  hasError = false;

  ngOnInit(): void {
    this.weatherService.getCurrent().subscribe({
      next: (weather) => {
        this.weather = weather;
        this.isLoading = false;
      },
      error: () => {
        this.hasError = true;
        this.isLoading = false;
      }
    });
  }

  get description(): string {
    return this.weather ? describeWeatherCode(this.weather.weatherCode) : '';
  }
}
