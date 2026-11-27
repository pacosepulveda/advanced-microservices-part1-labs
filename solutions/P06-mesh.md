# P06 — Solución de referencia
Referencia: [soluciones-finales](https://github.com/pacosepulveda/advanced-microservices-part1-labs/tree/soluciones-finales). Service selector app: p06-inventory, sin version; dos endpoints Ready. DestinationRule host p06-inventory y subsets v1/v2 con labels version correspondientes. VirtualService hosts p06-inventory, gateways mesh, destinos mismos host/subsets, pesos 100/0 → 80/20 → 0/100. No modificar namespace entero; inyección por template después de preflight. Prueba determinista 100/0 y 0/100; reparto 80/20 aproximado con 100 muestras. Linkerd usa HTTPRoute Service parent, backendRefs a p06-inventory-v1/v2, pesos; check proxy y viz edges. Misma lógica de Inventory en ambas versiones; APP_VERSION distingue metadata, no es release de negocio diferente. Rúbrica: manifiestos válidos, evidencia endpoints, routing observado, comparación y limpieza. No confundir estado Accepted con éxito observado. CI Linkerd independiente iniciada; AKS requiere ensayo específico de Parte 1 y capacidad.


## Aclaración editorial 2026-11-17
La referencia P06 tiene solución 100/0 para ambos meshes; las fases 80/20 y 0/100 se obtienen cambiando pesos y revalidando tráfico. La CI anterior de Linkerd en kind validó 100/0 y 0/100; **no equivale a ensayo del instructor en Codespaces ni a validación de AKS Parte 1**. La disponibilidad de imágenes, permisos de VirtualService/DestinationRule en el namespace y revisión Istio se comprobarán después de Parte 2. Los manifiestos resueltos están disponibles en esta rama; no conceder permisos globales.

## Ajustes de esta edición

Los dos recursos Istio llevan `exportTo: ["."]` por la política de admisión del clúster. `scripts/p06-sample.sh` admite student01–student27 e instructor. Los manifiestos de esta rama están resueltos con pesos 100/0; cambia los pesos a 80/20 y 0/100 para repetir las comprobaciones.
