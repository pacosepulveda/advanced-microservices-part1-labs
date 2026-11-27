> **v0.3 — referencia principal:** [Código completo P01–P05](https://github.com/pacosepulveda/advanced-microservices-part1-labs/tree/soluciones-finales). El starter v0.3 deja HttpClientConfig y políticas incompletas. La solución completa (incluida clasificación 502/503/504) está en la rama de referencia; no copiar la clasificación antigua de todos los 5xx. make p02-test/verify. El material anterior se conserva abajo como explicación de los mecanismos, no como secuencia del starter.

# P02 — Solución de referencia (referencia)

## Secuencia
Usar guía P02 y comandos exactos del repo alumno. Restaurar variables por fase; no exportarlas. Conservar evidencias. Baseline de seis servicios CI verde antes de P02; las pruebas nuevas se verifican por separado.

1. Timeout: Inventory 2000 ms con read 5000 → 200 aproximadamente 2 s; read 500 → 502 y attempts delta 1. Tolerancia por carga y arranque. No confundir timeout con cancelación.
2. Retry: 503 upstream, maxAttempts 3 → 502 externo y delta 3. SKU-999 → 404 upstream, 502 externo, delta 1. Clasificación inicial acepta todos los 5xx, alumno restringe.
3. CB: 4 fallos, estado OPEN, quinta devuelve 503 CIRCUIT_OPEN y attempts se mantiene 4. Restaurar Inventory sin recrear Orders; después de 15 s, dos llamadas sanas → CLOSED. Logs muestran OPEN→HALF_OPEN→CLOSED.
4. Bulkhead: 2 permisos y 8 peticiones sincronizadas; esperamos 2 éxitos/6 rechazos aproximadamente (depende de scheduling), menos de 8 intentos y 503 BULKHEAD_FULL. Sin protección, 8 intentos. Ping independiente sano; no prueba aislamiento total.
5. Composición: original CircuitBreaker dentro de Retry. Con 503 continuo, maxAttempts=3, ventana 4, primera operación 3 fallos; segunda llega al cuarto, abre y siguiente intento se rechaza. CB fuera de Retry: cada operación agotada cuenta como 1 fallo, por lo que requiere 4 operaciones fallidas. Mantener Bulkhead externo en ambos. Medir antes de concluir.
 
## Cambio de código CORE (clasificación)
```java
static boolean isTransient(Throwable ex) {
    if (!(ex instanceof InventoryUnavailableException)) return false;
    Throwable cause = ex.getCause();
    if (cause instanceof ResourceAccessException) return true;
    if (cause instanceof HttpServerErrorException http) {
        int status = http.getStatusCode().value();
        return status == 502 || status == 503 || status == 504;
    }
    return false;
}
```
Ejemplo test (en InventoryResilienceTest, imports HttpStatus y HttpServerErrorException):
```java
@Test void onlySelectedServerErrorsRetry() {
    var p = new InventoryResilience(true,false,false,3,1,5000,2);
    assertThatThrownBy(() -> p.execute(() -> {
        throw new InventoryUnavailableException("bad",
            new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    })).isInstanceOf(InventoryUnavailableException.class);
    assertThat(p.snapshot()).containsEntry("attempts",1L);
    assertThatThrownBy(() -> p.execute(() -> {
        throw new InventoryUnavailableException("busy",
            new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));
    })).isInstanceOf(InventoryUnavailableException.class);
    assertThat(p.snapshot()).containsEntry("attempts",4L);
}
```
El contador final 4 es acumulado (1 + 3). La política es ejemplo, no recomendación universal. La clasificación afecta también ignoreException del circuito.

## Cambio de composición
Intercambiar únicamente estas dos líneas (dejar Bulkhead al final):
```java
if (retryEnabled) call = Retry.decorateSupplier(retry, call);
if (circuitEnabled) call = CircuitBreaker.decorateSupplier(circuit, call);
```
Ahora CB cuenta operaciones lógicas, no intentos. No modificar clase base ni contadores. Restaurar tras experimento.

## Rúbrica de cierre
- 1: explica timeout y evidencia real; 2: cuenta intentos y clasifica fallos; 3: demuestra apertura y recuperación; 4: demuestra rechazo/aislamiento limitado; 5: compara composición con evidencia; 6: propone parámetros y reconoce límites.
- Para 1,5 s, ejemplo dos intentos de 500 ms + 100 ms de backoff, margen nominal 400 ms. No es SLA; medir latencia p95/p99 y coordinar deadline/capas. Con 3 intentos de 500 ms + 2 esperas, ya supera 1,5 s nominal.
- No aceptar solo PASS. Pedir predicción, evidencia y trade-off.
