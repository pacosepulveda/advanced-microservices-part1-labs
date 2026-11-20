# Advanced Orders Platform — Microservicios avanzados, Parte 1

**Checkpoint P00/P01.** Comienza por P00 y desarrolla P01 desde los TODOs.

## Antes de comenzar
Lee la [guía general del alumno](labs/GUIA-ALUMNO.md) para acceder, preparar Codespaces, guardar cambios y avanzar entre checkpoints.

## Trabaja en Codespaces
Abre el checkpoint indicado por el instructor en tu repositorio de alumno. Conserva tu trabajo anterior en una rama/commit propio. No instales herramientas en tu ordenador. Abre terminal en la raíz y ejecuta:
```bash
git status --short --branch
make preflight
make test
```
Sigue el enunciado de tu práctica para arrancar el overlay correcto. El entorno usa Java 21, Maven, Spring Boot 4.1.2, Spring Cloud 2025.1.4 y Docker Compose. La aplicación es didáctica: no hay pagos, envíos, notificaciones ni datos de clientes reales. Los puertos de Compose se vinculan a loopback; mantén privados los puertos de Codespaces.

## Guías disponibles en este checkpoint
- [P00 — Primer contacto](labs/p00-getting-started/README.md)
- [P01 — Estimación de pedido](labs/p01-architecture/README.md) **(actual)**

## Entorno y cierre
- P00/P01: `make up` inicia Gateway, Orders e Inventory; `make p01-up` añade Shipping. `make down` detiene el entorno base y extended.
- Cada overlay posterior tiene instrucciones propias de arranque y parada en su guía. No mezcles overlays ni borres volúmenes sin guardar evidencias.
- Las pruebas de regresión (`make test`) y las de aceptación del ejercicio son distintas. Un TODO puede provocar un fallo esperado de aceptación; un fallo de compilación o entorno no es evidencia de que el patrón esté incompleto.
- P06 requiere herramientas de Kubernetes, contexto y namespace asignados; no se necesitan credenciales Azure para los demás ejercicios.
- Los checkpoints se entregan por etapas. Guarda cambios antes de pasar a otro; no uses reset destructivo ni copies soluciones de compañeros.

## Enunciados de todo el curso

Consulta el [índice de P00–P07](docs/enunciados/README.md) desde el primer día. **Los enunciados están disponibles antes que el código:** las implementaciones, los TODO y la solución de la práctica anterior se liberan por checkpoints. No intentes ejecutar una práctica cuyo código todavía no se ha publicado.
