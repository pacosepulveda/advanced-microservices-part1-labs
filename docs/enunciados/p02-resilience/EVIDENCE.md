# P02 — Evidencias
- Rama / SHA:
- Diagrama de dependencia y política:
| Experimento | Hipótesis | HTTP / código | Tiempo | attempts (delta) | Estado / permisos |
|---|---|---|---|---|---|
| Baseline | | | | | |
| Demora sin timeout | | | | | |
| Timeout 500 ms | | | | | |
| Retry 503 | | | | | |
| SKU inexistente | | | | | |
| CB 4 fallos + rechazo | | | | | |
| CB recuperación | | | | | |
| Burst sin/con Bulkhead | | | | | |
| Composición A/B | | | | | |
- Pruebas make p02-test / make p02-verify:
- Test propio de composición:
- ADR: decisión, presupuesto, alternativa y trade-off:
- Commit/diff:
