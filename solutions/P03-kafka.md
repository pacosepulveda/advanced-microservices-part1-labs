> **v0.3 — referencia principal:** [Código completo P01–P05](https://github.com/pacosepulveda/advanced-microservices-part1-labs/tree/soluciones-finales). El alumno implementa todo publish y consume; la solución incluye productor completo, listener y dedup, no solo una línea. make p03-verify exige un efecto por dos entregas sin variable de expectativa. El material anterior se conserva abajo como explicación de los mecanismos, no como secuencia del starter.

# P03 — Solución de referencia (en validación técnica)

## Resultado esperado
- Topic orders.created.v1, 3 particiones, replication factor 1. Broker KRaft single node. El ACK de publicación incluye topic, partition, offset, no prueba efecto.
- Dos publicaciones con el mismo eventId/payload producen dos registros (offsets distintos). El starter no implementa todavía el listener: el alumno observa received=2/effects=2 **solo después de escribir una primera versión ingenua** del consumidor, antes de aplicar deduplicación.
- Detener Notifications no bloquea publicación mientras broker está sano; al arrancar consumidor, procesa desde offset del grupo. Diagnósticos en memoria reinician.
- Cambio CORE: en OrderEventConsumer.consume sustituir incremento de effects por:
```java
effects.putIfAbsent(event.eventId(), new AtomicInteger(1));
```
Mantener incremento received para evidencias. Inserción atómica solo del efecto contador; no es una transacción con correo/DB.
- Reconstruir Notifications con overlay y ejecutar **make p03-verify**. El script vigente exige `received=2` y `effects=1`; la variable histórica `P03_EXPECT_EFFECTS` ya no se utiliza. No cambiar starter main: es deliberadamente incompleto.
- Pregunta de diseño: persistir clave única (consumer, eventId) y efecto local en una transacción. Reconocer offset después. Para efecto externo, outbox/proveedor idempotente/reconciliación. No prometer exactly-once global.

## Rúbrica
1. Identifica partición y offset, distingue ACK y consumo.
2. Demuestra desacoplamiento temporal y recuperación.
3. Demuestra dos registros y dos efectos antes, dos entregas y un efecto después.
4. Explica por qué idempotencia de productor no resuelve duplicado de negocio.
5. Propone solución persistente y explica caída entre efecto y commit.
No aceptar solo PASS; pedir observación, razonamiento y registro de evidencias reales. Los resultados esperados se contrastarán durante el ensayo Codespaces.
