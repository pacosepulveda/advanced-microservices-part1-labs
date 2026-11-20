# P07 — Pistas progresivas
Abre solo la pista que necesites.
## Nivel 1
Compara aggregate, events, query y stock persistente. ¿Qué representación es autoritativa para cada servicio? Una query desactualizada no explica necesariamente una discrepancia del participante.
## Nivel 2
Correlaciona los eventos del pedido con inventory.reservation y payments.decision. ¿El último hecho registrado en Orders tiene evidencia de un efecto confirmado en Inventory?
## Nivel 3
Lee advance y los helpers del participante. Localiza la transición inmediatamente anterior a CANCELLED y comprueba las condiciones que permiten registrar el siguiente evento. Contrasta con el modelo de P04.
## Recuperación
- Si exit no es 42 antes del cambio, confirma rama, rebuild, stock >=4 y overlays. Un HTTP 500 no reproduce el defecto esperado.
- Si query 404, project manual; usa aggregate e historial para investigar. No editar filas.
- Tras corregir usa un ID nuevo, verifica camino exitoso y cancelación, y revisa logs si falla. No modificar el verificador.
- Si el stock se agota, guarda evidencia, confirma que el volumen es tuyo y desechable, y solo entonces docker compose -f docker-compose.yml -f compose.state.yml --profile extended down -v seguido de make p04-up. Elimina historial del sandbox; no es solución al incidente.
