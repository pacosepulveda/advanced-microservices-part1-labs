# P03 — Evidencias y decisiones
- Rama/SHA:
- Diagrama y contrato (eventId vs orderId vs offset):
- Topic, particiones y factor de replicación:
| Caso | eventId | key | partición/offset | HTTP | received | effects |
|---|---|---|---|---|---:|---:|
| Primer ACK (sin listener) | | | | | | |
| Duplicado inicial | | | | | | |
| Duplicado corregido | | | | | | |
| Consumidor offline | | | | | | |
| Recuperación | | | | | | |
- Lag y logs observados:
- Prueba propia JUnit:
- make p03-verify:
- Diferencia ACK/commit/efecto:
- Decisión Kafka vs RabbitMQ y riesgo de deduplicación en memoria:
- Commit/diff:
