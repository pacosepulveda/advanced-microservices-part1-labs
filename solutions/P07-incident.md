# P07 — Solución de referencia
Consulta después del intento. En student-p07 se introdujo regresión en OrderStateController.advance: PAYMENT_FAILED devuelve kind RELEASED sin invocar participante. Fold aplica evento válido sintácticamente, pero el hecho es falso: reserva sigue RESERVED y stock no vuelve. Verificador reproduce salida 42 específica; no basta con HTTP 200 o CANCELLED.
## Evidencias
1. Historial CREATED, RESERVED, PAYMENT_DECLINED, RELEASED; agregado CANCELLED v4.
2. Stock antes 42 (o valor actual), tras cancelación dos unidades menos; tabla inventory.reservation status RESERVED para ID afectado. Query puede ser retrasada pero aggregate y stock demuestran problema incluso reconstruyendo.
3. Código de advance omite release(s); logs/SQL complementan evidencia.
## Corrección
```java
case "PAYMENT_FAILED" -> {
    if(!"RELEASED".equals(release(s)))
        throw new IllegalStateException("Release not confirmed");
    yield "RELEASED";
}
```
No mantener transacción DB durante HTTP. Si falla participante, no registrar RELEASED: estado PAYMENT_FAILED permanece y puede reintentarse. Inventory release idempotente con mismo ID. Si efecto se confirma pero append falla, repetir obtiene RELEASED sin incremento doble. Usar ID nuevo para verificar; histórico afectado requiere reconciliar reserva y evento, no editar historial ni liberar stock sin contrastar.
Referencia [soluciones-finales](https://github.com/pacosepulveda/advanced-microservices-part1-labs/tree/soluciones-finales). Verificador scripts/verify-p07.sh: antes exit42, después PASS y éxito confirmado. Pedir prueba propia del alumno (p. ej. llamar release repetido tras cancelación o comprobar reserva persistida; test nuevo que falle ante la regresión). Rúbrica 5 puntos: hipótesis, evidencias, fix, aceptación/regresión, ADR y riesgos.
