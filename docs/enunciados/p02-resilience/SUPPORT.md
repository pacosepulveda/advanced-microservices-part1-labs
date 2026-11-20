# P02 — Pistas y recuperación
Abre una pista cuando te bloquees; no necesitas leerlas todas. Los objetivos de aceptación no cambian.
## Timeout
- Nivel 1: busca la fábrica RestClient en HttpClientConfig, no el controller.
- Nivel 2: HttpClient.newBuilder().connectTimeout(Duration.ofMillis(...)).build(); JdkClientHttpRequestFactory recibe ese cliente.
- Nivel 3: setReadTimeout y requestFactory en RestClient.Builder; conserva baseUrl. No confundas milisegundos con segundos.
## Retry
- Nivel 1: identifica la excepción envuelta por InventoryClient.fetch. Orders responde 502 para causas distintas.
- Nivel 2: isTransient comprueba InventoryUnavailableException, getCause(), instanceof ResourceAccessException o HttpServerErrorException; getStatusCode().value() para 502/503/504.
- Nivel 3: RetryConfig.custom().maxAttempts(...).waitDuration(...).retryOnException(...).build(). Retry.decorateSupplier recibe política y Supplier. No ejecutar Supplier antes del decorador.
## Circuit Breaker
- Nivel 1: cuatro fallos en cuatro llamadas abren; una excepción ignorada no cuenta como éxito ni fallo.
- Nivel 2: CircuitBreakerConfig.custom; ventana, mínimo, umbral, espera, probes. ignoreException(ex -> !isTransient(ex)).
- Nivel 3: CircuitBreaker.decorateSupplier; comprueba flag y conserva event publishers. Recupera Inventory, no recrees Orders.
## Bulkhead y composición
- Nivel 1: semáforo limita concurrencia, no tasa. maxWaitDuration cero.
- Nivel 2: BulkheadConfig.custom; Bulkhead.decorateSupplier. Rechazo no ejecuta fetch.
- Nivel 3: el último wrapper es el externo. Para la referencia, primero CB alrededor de fetch, luego Retry, luego Bulkhead. No pases CallNotPermittedException al predicado como fallo reintentable.
## Diagnóstico
- Variables P02_* se leen al recrear Orders; no uses restart para cambiar entorno. Variables TRAINING_INVENTORY_* se leen al recrear Inventory. Usa asignación inline, no export persistente.
- Si aparece HTTP 000 o timeout curl, no es un HTTP 502; revisar logs y puertos. Si STATS falla, comprueba TRAINING_RESILIENCE_DIAGNOSTICS=true y Gateway.
- attempts se reinicia al recrear Orders; no comparar totales de procesos diferentes. La consulta STATS no incrementa. Los flags dicen configuración, no demuestran que el decorador se aplique.
- make p02-test comprueba Java, make test regresión, make p02-verify integración. Rebuild Orders antes del último; el script detiene entorno. make up + make verify recuperan.
- Si burst falla, guarda salidas y logs; confirma delay 2000, bulkhead activo, read timeout 5000 y Python disponible. No ocultar fallo cambiando aserciones.
- Si cambiaste de terminal, vuelve a definir URL/STATS; usa la rama de trabajo P02, no la referencia.
