# P02 — Extensiones (solo si acabas CORE)
1. Diseña backoff exponencial y jitter; explica cómo evitar tormentas de reintentos. Propón límites y una prueba determinista, sin prometer cumplimiento de deadline global.
2. Compara Circuit Breaker por intentos y operaciones lógicas con un test de secuencia; considera qué métrica usaría el equipo de operaciones.
3. Traslada la política al cliente Shipping sin compartir estado de circuito/bulkhead con Inventory. Justifica políticas por dependencia y añade prueba de aislamiento.
4. Modela riesgo de reintentar una operación de escritura: idempotency key y resultado incierto. No ejecutar pagos reales ni agregar un segundo mecanismo de retry en Gateway/mesh sin presupuesto.
