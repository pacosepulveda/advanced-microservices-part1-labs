# P04 — Evidencias y ADR
- Rama/SHA y estado del entorno:
- Modelo de agregado, comandos/eventos e invariantes:
- Diagrama CQRS y Saga:
- Pruebas fold y prueba propia:
| Pedido / caso | Evento / versión | Agregado | Query | Stock antes/después |
|---|---|---|---|---|
| Crear | | | | |
| Comando repetido/conflicto | | | | |
| Reserva | | | | |
| Pago rechazado | | | | |
| Compensación | | | | |
| Terminal repetido | | | | |
| Éxito | | | | |
| Reserva rechazada | | | | |
| Replay y reinicio | | | | |
| Ventana fallo remoto/append | | | | |
- make p04-verify:
- Commit/diff:
## ADR
- Contexto y requisito:
- Decisión (CQRS / ES / Saga o alternativa):
- Alternativa CRUD/transacción local y por qué:
- Garantías demostradas y riesgos restantes (HA, outbox, persistencia, aislamiento):
