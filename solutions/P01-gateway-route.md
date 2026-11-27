# P01 — Solución de referencia (pendiente de validación)

El objetivo es añadir /stock/{sku} sin modificar Inventory ni las rutas existentes. En services/api-gateway/src/main/resources/application.yml, bajo spring.cloud.gateway.server.webflux.routes, añadir:

```yaml
            - id: stock-alias
              uri: ${INVENTORY_URL:http://localhost:8081}
              predicates:
                - Path=/stock/{sku}
              filters:
                - SetPath=/api/inventory/{sku}
```

El predicado captura el segmento sku; el filtro SetPath sustituye el path antes del envío al upstream. No confundir con un redirect al cliente ni con un cambio de contrato de Inventory.

```bash
docker compose up -d --build --wait api-gateway
curl -i 'http://127.0.0.1:8080/stock/SKU-001'
curl -i 'http://127.0.0.1:8080/api/inventory/SKU-001'
curl -i 'http://127.0.0.1:8080/stock/SKU-999'
```

Esperado: 200 para las dos primeras, 404 para la tercera; confirmar mediante ejecución, no dar por hecho. Recuperar ruta inicial retirando solo el bloque stock-alias, reconstruir Gateway y ejecutar make verify.

Debrief: Gateway desacopla la URL externa de la interna, pero no elimina acoplamiento semántico ni disponibilidad síncrona. La latencia de Inventory sigue afectando al preview. En la siguiente práctica introduciremos mecanismos de resiliencia.
