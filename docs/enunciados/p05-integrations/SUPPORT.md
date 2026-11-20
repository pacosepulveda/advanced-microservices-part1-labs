# P05 — Pistas y recuperación
## Webhook
- Nivel 1: valida antes de efectos. ObjectMapper.readValue(raw,Event.class); captura error de deserialización y usa ResponseStatusException BAD_REQUEST.
- Nivel 2: eventId [A-Za-z0-9-]{1,64}, type shipping.delivered, orderId no vacío. received.computeIfAbsent + incrementAndGet; effects.putIfAbsent para marcador único simulado.
- Nivel 3: prueba sin secreto con new WebhookController(ObjectMapper,""), byte[] UTF-8 y firma null. stats(id) permite comprobar mapa. La firma se comprueba antes de parsear el body.
## RPC
- QuoteReply conserva números 1 y 2; añade 3..6 según contrato. cmp ambos proto; no editar target.
- ShippingPricing.quote(request.getDestination(),request.getWeightGrams()); inspecciona record de salida. QuoteReply.newBuilder y setters camelCase; onNext + onCompleted o onError, no ambos.
- Cliente: QuoteRequest.newBuilder; stub.withDeadlineAfter; StatusRuntimeException.getStatus().getCode(); ResponseEntity.status(http).body(Map.of(...)).
- Si Maven no encuentra getters, regenera ambos módulos y revisa imports de clases generadas. Si HTTP 501, comprueba TODO y rebuild. Si 503, verifica shipping:9090 y overlay; si aparece tras recrear, espera reconexión. Si 504, compara delay/deadline.
- HMAC usa bytes exactos, SHA256 y hexadecimal; una firma sobre JSON reserializado puede no coincidir. No usar secreto real. Comprueba openssl si falla aceptación.
- make p05-up reconstruye; make p05-verify exige solución y restaura delay/secreto en camino exitoso. Si se interrumpe, recrea shipping y notifications sin variables P05_* o ejecuta make p05-down y make p05-up.
