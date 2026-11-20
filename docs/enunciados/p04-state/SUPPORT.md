# P04 — Pistas progresivas y recuperación
## Event Sourcing
- Nivel 1: history ya devuelve eventos ordenados. fold no consulta BD ni participantes. Empieza con estado NONE y versión esperada 1.
- Nivel 2: usa un bucle y un switch por estado para validar el siguiente kind; otro switch puede mapear kind a estado. Conserva first.sku/quantity/decline.
- Nivel 3: IllegalStateException para transición o versión inválida; constructor State(id,sku,quantity,decline,status,version). No volver a aplicar efectos. El historial vacío ya produce 404.
## CQRS
- Nivel 1: aggregate(id) reconstruye State; query(id) lee tabla. project conecta ambos.
- Nivel 2: INSERT INTO orders.read_model (order_id,sku,quantity,status,version) VALUES (?,?,?,?,?) ON CONFLICT(order_id) DO UPDATE SET ... WHERE orders.read_model.version < EXCLUDED.version.
- Nivel 3: try(var c=connect(); var p=c.prepareStatement(sql)) { ... }; setString/setInt, executeUpdate; luego query(id). No construir SQL concatenando entradas.
## Saga
- Nivel 1: usa estado autoritativo agregado, no read_model. Un advance ejecuta un paso.
- Nivel 2: reserve/decidePayment/release devuelven status; mapea únicamente respuestas válidas. El estado PAID solo registra CONFIRMED.
- Nivel 3: recordTransition(s,kind) después de respuesta confirmada. Terminal devuelve s. Si PAYMENT_FAILED, exige RELEASED antes de registrar evento; ante fallo no inventes éxito.
## Diagnóstico
- make p04-test comprueba fold; make p04-verify exige las tres piezas implementadas y reutiliza contenedores. Reconstruye Orders tras cambios.
- Si POST commands falla por TODO, fold aún no funciona; create hace rollback local. No es un error del esquema si preflight y baseline están bien.
- Si query devuelve 404 mientras aggregate funciona, falta proyección. Si query está atrasado, project manual. No usar query para tomar decisiones de Saga.
- Si stock no cambia: endpoint persistente /api/inventory/training/state/stock/SKU-001, no /api/inventory/SKU-001 del fixture.
- Si error SQL: revisa logs y schema. Para inspección de solo lectura en sandbox: docker compose -f docker-compose.yml -f compose.state.yml --profile extended exec -T postgres psql -U lab -d advms -c 'SELECT order_id,version,kind FROM orders.event ORDER BY order_id,version;' . No modificar tablas para hacer pasar tests.
- Si reserva ya existe: usa ID nuevo para nuevos pedidos; repetir mismo ID/payload no debe decrementar otra vez. Conflicto indica payload distinto. El verificador consume 2 unidades por éxito. Si stock insuficiente, guarda evidencias y detén el sandbox; solo si confirmas que es tu volumen desechable, docker compose -f docker-compose.yml -f compose.state.yml --profile extended down -v, luego make p04-up. Esto elimina todo historial del sandbox.
- Si terminal nueva: restablece C=(docker compose -f docker-compose.yml -f compose.state.yml --profile extended), BASE, OS, IS e IDs guardados. make p04-down conserva volumen. No mezclar overlays ni tocar Azure.
