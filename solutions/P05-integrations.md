# P05 — Solución de referencia comentada

**Referencia editorial vigente:** starter `editorial-p05`, enunciado P05 y script `scripts/verify-p05.sh`. La rama pública de soluciones [soluciones-finales](https://github.com/pacosepulveda/advanced-microservices-part1-labs/tree/soluciones-finales) conserva la implementación de webhook, proto y RPC. Este documento se entrega al cierre del curso. **Pendiente de ensayo interactivo del instructor** desde starter, incluso si las pruebas automáticas de referencia pasaron. Sin estimaciones de duración de prácticas.


## Estado exacto del starter

1. WebhookController.delivery recibe el body como byte[] y ya comprueba tamaño máximo 8192 y HMAC opcional. Después devuelve HTTP 501 por un TODO: todavía **no** hay dos entregas/efectos simulados.
2. Los dos `shipping.proto` incluyen QuoteRequest completo, pero QuoteReply solo tiene destination=1 y weight_grams=2. Faltan los campos 3..6.
3. ShippingRpcServer inicia y cierra el servidor gRPC y expone Quote. El método devuelve UNIMPLEMENTED hasta completarlo.
4. ShippingRpcController ya crea el ManagedChannel, blocking stub y valida deadlineMs (50..5000). La ruta HTTP devuelve HTTP 501 y grpcCode UNIMPLEMENTED hasta que el alumno implemente la consulta.
5. `compose.integrations.yml` activa servicios/flags. `make p05-up` reconstruye contenedores. `make p05-verify` ejecuta aceptación actual sin variables externas de expectativas.

**Eliminar del criterio de corrección anterior:** no usar `P05_EXPECT_EFFECTS=1`, porque no existe en el verificador actual. Tampoco se solicita alterar artificialmente `Math.min(deadlineMs,1000)`; corresponde a otra iteración y no al starter vigente. La referencia acumulada conserva un comentario TODO sobre política de deadline, pero su implementación funcional utiliza `withDeadlineAfter(deadlineMs,...)`; no tratar ese comentario como tarea adicional del alumnado.

## 1. Webhook — solución de referencia

El método debe deserializar bytes con `tools.jackson.databind.ObjectMapper`, rechazar JSON inválido y campos fuera del contrato **antes** de incrementar contadores. El identificador de evento representa el hecho lógico, no el pedido:

~~~java
Event e;
try {
    e = json.readValue(raw, Event.class);
} catch (Exception ex) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON");
}
if (e == null || e.eventId() == null
        || !e.eventId().matches("[A-Za-z0-9-]{1,64}")
        || !"shipping.delivered".equals(e.type())
        || e.orderId() == null || e.orderId().isBlank())
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event");

received.computeIfAbsent(e.eventId(), k -> new AtomicInteger()).incrementAndGet();
effects.putIfAbsent(e.eventId(), new AtomicInteger(1));
return stats(e.eventId());
~~~

Dos POST con el mismo `eventId` nuevo producen `received=2, effects=1`. JSON inválido o type incorrecto dan HTTP 400 sin efecto. El código no compara payloads distintos con mismo ID ni mantiene marcadores persistentes; el mapa se borra al recrear el proceso y no garantiza un único correo real.

La validación HMAC existente se calcula sobre los bytes originales cuando `secret` no está vacío. Cabecera de ejemplo: `X-Lab-Signature`. Ausente/incorrecta: HTTP 401; 8192+ bytes: 413 según la comprobación. HMAC no cifra ni impide por sí solo replay; secretos de laboratorio únicamente.

**Prueba propia requerida:** construir WebhookController con ObjectMapper y `""`, invocar dos veces con `byte[]` UTF-8 del mismo evento, comprobar 2/1; enviar un type inválido y comprobar 400 sin cambio de stats. Es una prueba unitaria, no del Gateway.

## 2. Protobuf — contrato completo en ambos módulos

~~~proto
syntax = "proto3";
package advancedorders.shipping.v1;
option java_package = "com.advancedorders.shipping.rpc";
option java_multiple_files = true;

service ShippingQuotes {
  rpc Quote (QuoteRequest) returns (QuoteReply);
}
message QuoteRequest {
  string destination = 1;
  int32 weight_grams = 2;
}
message QuoteReply {
  string destination = 1;
  int32 weight_grams = 2;
  int64 price_cents = 3;
  string currency = 4;
  int32 estimated_days = 5;
  string provider = 6;
}
~~~

Copiar el **mismo contrato revisado** a:
`services/orders/src/main/proto/shipping.proto` y `services/shipping/src/main/proto/shipping.proto`.

~~~bash
cmp services/orders/src/main/proto/shipping.proto services/shipping/src/main/proto/shipping.proto
mvn -B -pl services/orders,services/shipping -am generate-sources
~~~

No editar fuentes generadas; conservar los números de campo y versionar el contrato en un proyecto real.

## 3. Servidor gRPC — reutilizar ShippingPricing

Dentro del método `ShippingRpcServer.quote`:

~~~java
try {
    var q = ShippingPricing.quote(request.getDestination(), request.getWeightGrams());
    if (delayMs > 0) Thread.sleep(delayMs);
    if (Context.current().isCancelled()) return;
    observer.onNext(QuoteReply.newBuilder()
        .setDestination(q.destination())
        .setWeightGrams(q.weightGrams())
        .setPriceCents(q.priceCents())
        .setCurrency(q.currency())
        .setEstimatedDays(q.estimatedDays())
        .setProvider(q.provider()).build());
    observer.onCompleted();
} catch (IllegalArgumentException e) {
    observer.onError(Status.INVALID_ARGUMENT
        .withDescription(e.getMessage()).asRuntimeException());
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    observer.onError(Status.CANCELLED.asRuntimeException());
}
~~~

El método ShippingPricing.quote es la fuente de precio utilizada también por REST; para ES/500 el valor esperado es 499 céntimos EUR, tres días y SIMULATED. La demora y la cancelación son simulación didáctica; no afirmar cancelación transaccional de efectos remotos.

## 4. Cliente gRPC y adaptador HTTP de Orders

El constructor de ShippingRpcController ya crea y gestiona `ManagedChannel` con `shipping:9090` y `ShippingQuotesBlockingStub`. Dentro del método HTTP `quote`, después de validar deadlineMs:

~~~java
try {
    var req = QuoteRequest.newBuilder()
        .setDestination(destination)
        .setWeightGrams(weightGrams)
        .build();
    var q = stub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS).quote(req);
    return ResponseEntity.ok(Map.of(
        "destination", q.getDestination(),
        "weightGrams", q.getWeightGrams(),
        "priceCents", q.getPriceCents(),
        "currency", q.getCurrency(),
        "estimatedDays", q.getEstimatedDays(),
        "provider", q.getProvider(),
        "grpcCode", "OK"));
} catch (StatusRuntimeException e) {
    var code = e.getStatus().getCode();
    var http = switch (code) {
        case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
        case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
        case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
        default -> HttpStatus.BAD_GATEWAY;
    };
    return ResponseEntity.status(http).body(
        Map.of("grpcCode",code.name(),"message","RPC failed"));
}
~~~

El mapeo HTTP lo decide el adaptador; no es una equivalencia universal de gRPC. El cliente curl llama a Orders por HTTP; el tráfico gRPC real es Orders→Shipping y usa el puerto interno 9090 sin TLS **solo** en el sandbox.

## 5. Evidencias exigibles antes de cerrar

~~~bash
make p04-down
make p03-down
make p05-up
C=(docker compose -f docker-compose.yml -f compose.integrations.yml --profile extended)
BASE=http://127.0.0.1:8080/api
HOOK="$BASE/notifications/training/webhooks/delivery"
RPC="$BASE/orders/training/grpc/quote"
make test
make p05-verify
make p05-down
~~~

Comprobar y registrar:
- Webhook inicial 501 → posterior HTTP 200 con dos entregas/único efecto, e inválidos HTTP 400.
- Dos contratos Protobuf iguales y generación de fuentes sin edición manual.
- REST y gRPC devuelven precio 499 para ES/500; RPC inválido `grpcCode=INVALID_ARGUMENT`, HTTP 400.
- Shipping con demora 1500 ms: deadline 300 ms → `DEADLINE_EXCEEDED`, HTTP 504; tras restaurar → OK.
- Firma sin secreto (opcional) y con secreto ficticio: sin firma HTTP 401, HMAC válido HTTP 200.
- Verificador actual termina `P05 webhook, gRPC quote, deadline, recovery and HMAC passed`.
- Probar y explicar al menos un caso nuevo, y registrar diff/commit y decisiones de seguridad.

**No confundir el paso del verificador en la rama de referencia con haber completado el ejercicio desde el starter en la preparación personal del instructor.** El cierre depende del ensayo y de revisar las incidencias reales.
