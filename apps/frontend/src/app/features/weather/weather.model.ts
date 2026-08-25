// Tipos del recurso Weather: espejo del contrato JSON del backend (GET /api/weather).
export interface Weather {
  city: string;
  temperature: number;
  humidity: number;
  /** Código WMO del estado del tiempo (0 despejado, 3 nublado, 61 lluvia, etc.). */
  weatherCode: number;
  windSpeed: number;
}

/** Rangos de código WMO → descripción corta en español. Es un resumen mínimo, no la tabla completa. */
const WEATHER_DESCRIPTIONS: { min: number; max: number; label: string }[] = [
  { min: 0, max: 0, label: 'Despejado' },
  { min: 1, max: 3, label: 'Parcialmente nublado' },
  { min: 45, max: 48, label: 'Niebla' },
  { min: 51, max: 67, label: 'Lluvia o llovizna' },
  { min: 71, max: 77, label: 'Nieve' },
  { min: 80, max: 82, label: 'Chubascos' },
  { min: 95, max: 99, label: 'Tormenta' }
];

/** Traduce un código WMO a texto legible; los códigos fuera de la tabla se muestran como "Desconocido". */
export function describeWeatherCode(code: number): string {
  const match = WEATHER_DESCRIPTIONS.find((range) => code >= range.min && code <= range.max);
  return match ? match.label : 'Desconocido';
}
