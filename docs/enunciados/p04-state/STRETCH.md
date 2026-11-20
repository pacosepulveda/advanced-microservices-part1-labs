# P04 — Extensiones
1. Diseña un Outbox para registrar el evento y una intención de publicación en la misma transacción local, con relay y consumidor idempotente. P03 y P04 no están integrados en este laboratorio.
2. Analiza dos orquestadores concurrentes: synchronized solo protege una JVM. Propón control optimista de versión, coordinación y tratamiento del efecto remoto previo al append; una restricción de BD no deshace HTTP.
3. Diseña una proyección automática con checkpoint, idempotencia y reconstrucción; compara con la proyección manual.
4. Modela snapshots y evolución de contratos de eventos. No editar historial para cambiar estado; plantea un evento correctivo y política de migración.
5. Analiza cuándo CRUD y transacciones locales serían suficientes, y qué coste aporta Event Sourcing.
