# Soluciones finales — Microservicios avanzados, Parte 1

Esta rama conserva los enunciados y el código acumulado resuelto de las prácticas P01–P07. P00 es de preparación; sigue [su guía](../labs/p00-getting-started/README.md). Las soluciones son referencias didácticas, no una arquitectura lista para producción. Verifica los resultados en tu propio sandbox.

- [P01-estimate](P01-estimate.md)
- [P01-gateway-route](P01-gateway-route.md)
- [P02-resilience](P02-resilience.md)
- [P03-kafka](P03-kafka.md)
- [P04-state](P04-state.md)
- [P05-integrations](P05-integrations.md)
- [P06-mesh](P06-mesh.md)
- [P07-incident](P07-incident.md)

Para reproducir el incidente P07 antes de aplicar la corrección, utiliza la rama `main`. Para comprobar el código corregido, utiliza esta rama y ejecuta `make p04-up`, `bash scripts/verify-p07.sh` y `make p04-down`.
