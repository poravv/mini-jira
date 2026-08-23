import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Weather } from './weather.model';

/**
 * Cliente HTTP del recurso weather (/api/weather, ruta relativa que resuelven el proxy de dev o nginx).
 * El backend es quien consulta la API externa de clima; el frontend solo pide el resultado.
 */
@Injectable({ providedIn: 'root' })
export class WeatherService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/weather';

  /** Clima actual de la ciudad configurada en el backend (Asunción). */
  getCurrent(): Observable<Weather> {
    return this.http.get<Weather>(this.baseUrl);
  }
}
