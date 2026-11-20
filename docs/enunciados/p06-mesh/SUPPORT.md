# P06 — Pistas y recuperación
- Service: selector app=p06-inventory; ambas versiones comparten label. EndpointSlices Ready antes de muestrear; targetPort=http.
- Istio: DestinationRule host coincide con Service; subsets labels version v1/v2. VirtualService usa mismos host/subsets y pesos total 100. Proxy de origen imprescindible; proxy nativo puede estar en initContainer restartPolicy Always.
- Linkerd: kind separado, Gateway API HTTPRoute parentRef Service; backendRefs Services por versión. No copiar VirtualService a Linkerd.
- Forbidden: confirmar contexto y namespace, permisos con instructor. No escalar ni cambiar RBAC de cluster. ImagePullBackOff: tag/ACR, o kind load. Sin endpoints: selector, readiness, namespace. Sin routing: proxy, host, labels, status Accepted y propagación. HTTP 200 no prueba mTLS.
- Herramientas: verificar kubectl, kind y linkerd; script local usa kubeconfig aislado. No ejecutar instalación sobre AKS. Si falla el piloto, conservar error y pedir instructor; no borrar cluster ajeno.
- Sample usa curl desde Orders y Python en Codespace; abre nuevas conexiones. 80/20 es aproximado; 100/0 y 0/100 son control determinista tras convergencia. No inferir balanceo por request persistente.
- Limpiar solo recursos p06 del namespace propio; nunca borrar namespace. Restablecer KUBECONFIG.
