# Reglas y estados de prioridad de las incidencias
## Condiciones de estado

1. Solo se podrá modificar el estado siguiendo un flujo; no se podrán saltear estados. Por ejemplo, si está en PENDIENTE, no se podrá pasar directamente a CERRADA. También se podrá retroceder; por ejemplo, de RESUELTA a EN_PROGRESO.

	Las transiciones serán: PENDIENTE -> EN_PROGRESO <-> RESUELTA -> CERRADA.

2. La prioridad sí se podrá modificar libremente, desde BAJA hasta CRÍTICA.

3. Solo ADMIN y SUPPORT podrán modificar las incidencias, el estado y la prioridad.

4. Los usuarios no podrán modificar las incidencias.

La prioridad se compone de BAJA, MEDIA, ALTA y CRÍTICA.

Los estados de las incidencias son PENDIENTE, EN_PROGRESO, RESUELTA y CERRADA.

La modificación se realizará en la misma pestaña donde se hacía manualmente hasta ahora. La diferencia es que se comprobará si la persona que modifica está autorizada y si la modificación es válida.