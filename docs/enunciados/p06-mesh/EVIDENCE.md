# P06 — Evidencias y ADR
- Rama/SHA y contexto (AKS propio / kind / piloto):
- Namespace, imágenes, revisión y preflight:
- EndpointSlices, labels y proxies Ready:
| Entorno | Política | Número muestras | v1 | v2 | Observación |
|---|---|---:|---:|---:|---|
| Service inicial | Sin política | | | | |
| Istio | 100/0 | | | | |
| Istio | 80/20 | | | | |
| Istio | 0/100 | | | | |
| Linkerd | 100/0 | | | | |
| Linkerd | 80/20 | | | | |
| Linkerd | 0/100 | | | | |
- HTTPRoute parent / Accepted y viz edges:
- Diagrama, diff YAML y ADR (ClusterIP/LoadBalancer, L4/L7, Istio/Linkerd, límites):
- Limpieza verificada:
