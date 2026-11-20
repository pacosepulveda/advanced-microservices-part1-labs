# P03 — Extensiones
1. Diseña deduplicación persistente con UNIQUE(event_id) y efecto local en la misma transacción. ¿Qué ocurre si el efecto es HTTP externo?
2. Analiza Outbox: escribir pedido y evento en una transacción local, publicar con relay y deduplicar. No afirmar que el laboratorio ya lo implementa.
3. Predice comportamiento de dos consumidores del mismo grupo y de grupos diferentes; compara con tres particiones. Explica orden por partición y efectos del cambio de key/particiones.
4. Diseña un contrato versionado compatible y una política de mensajes inválidos/DLQ; no modificar el tópico base ni borrar datos para probarlo.
