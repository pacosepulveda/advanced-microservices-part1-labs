> **v0.3 — referencia principal:** [Código completo P01–P05](https://github.com/pacosepulveda/advanced-microservices-part1-labs/tree/soluciones-finales). La referencia v0.3 contiene fold completo con validación, proyección y advance con helpers; make p04-test/verify. El alumno implementa las tres piezas. El material anterior se conserva abajo como explicación de los mecanismos, no como secuencia del starter.

# P04 — Solución de referencia (en validación)
Resultados esperados: CREATED v1 NEW; query 404 antes de project; project NEW v1; advance RESERVED v2, query sigue NEW v1; PAYMENT_DECLINED v3 PAYMENT_FAILED; RELEASED v4 CANCELLED; stock restaurado. Repetir release/advance no duplica efecto. Borrar read_model y proyectar devuelve CANCELLED v4 sin repetir HTTP. Camino exitoso CREATED, RESERVED, PAYMENT_APPROVED, CONFIRMED. Reserva rechazada por falta de stock produce CANCELLED. CI prueba happy/failure path, replay y reserva/liberación repetida.
## Cambio CORE: validación del historial
Extraer fold a método static package-private de OrderStateController (o clase pura). Antes del switch de estado, validar versión incremental y estado anterior:
```java
int expected=1;
String status="NONE";
for (Event e:events) {
    if(e.version()!=expected++) throw new IllegalStateException("Non-consecutive version");
    boolean allowed=switch(status) {
        case "NONE" -> e.kind().equals("CREATED");
        case "NEW" -> Set.of("RESERVED","RESERVATION_REJECTED").contains(e.kind());
        case "RESERVED" -> Set.of("PAYMENT_APPROVED","PAYMENT_DECLINED").contains(e.kind());
        case "PAID" -> e.kind().equals("CONFIRMED");
        case "PAYMENT_FAILED" -> e.kind().equals("RELEASED");
        default -> false;
    };
    if(!allowed) throw new IllegalStateException("Invalid transition");
    // Mantener el switch de estado existente.
}
```
Añadir test en mismo paquete con OrderStateController.Event y OrderStateController.fold (static), import java.util.List y AssertJ. CREATED→CONFIRMED debe lanzar; CREATED→RESERVED→PAYMENT_DECLINED→RELEASED debe devolver CANCELLED v4. Añadir test de salto de versión. No ejecutar efectos durante fold.
## Decisión
CQRS/ES independientes; usar CRUD cuando no se necesita historial autoritativo. Una DB con tres esquemas y credencial común es simplificación, no aislamiento productivo. JDBC directo, sin pool ni migraciones; orquestador manual de un proceso, no HA. Outbox no implementado. Proyección manual, no convergencia automática. No confundir catálogo fixture con stock persistente.
