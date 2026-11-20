# P03 — Pistas y recuperación
## Productor
- Nivel 1: el record ya define el contrato; el método publish debe generar JSON, enviar y responder solo cuando tiene metadatos.
- Nivel 2: KafkaTemplate.send(topic,key,value) devuelve un futuro; get(12,TimeUnit.SECONDS) entrega SendResult. Extrae getRecordMetadata(), partition() y offset(). El ObjectMapper es tools.jackson.databind, no el import antiguo de Jackson.
- Nivel 3: ResponseEntity.accepted().body(Map.of(...)); InterruptedException exige Thread.currentThread().interrupt(); traduce fallo a 503 sin afirmar que no se escribió nada.
## Consumidor
- Nivel 1: @KafkaListener(topics="orders.created.v1",groupId="notifications-p03"); record.value(), key(), partition(), offset(). El listener se registra por Spring, no es endpoint HTTP.
- Nivel 2: json.readValue(record.value(),OrderCreated.class); received.computeIfAbsent(...).incrementAndGet(); registra metadatos. El DTO está definido también en Notifications.
- Nivel 3: usa putIfAbsent para marcador de efecto único por eventId en el mapa concurrente. No confundas AtomicInteger con deduplicación durable ni containsKey + put con atomicidad.
## Prueba propia
- new ConsumerRecord<>("orders.created.v1",0,0L,"order-01",json) y otro con offset distinto; ambos llevan mismo JSON y eventId. Invoca consume dos veces y assertThat(snapshot(id)).containsEntry("received",2).containsEntry("effects",1). Instancia ObjectMapper del stack; revisa otros tests de Spring/JUnit.
## Incidencias
- POST 501: starter o imagen Orders sin rebuild. POST 503: revisa logs de Orders/Kafka, broker y topic. No inventar offset.
- GET 0/0: espera consumidor, verifica listener/anotación, grupo y logs. No republiques durante espera. GET 404: revisa overlay y ruta, no asumir falta de evento; snapshot normalmente devuelve 0/0.
- Broker no root: p03-up inicializa solo volumen propio; no hacer chown de directorios globales. Kafka usa kafka:9092 desde contenedores; localhost:9092 solo desde broker.
- Recrear Notifications reinicia mapas; offsets confirmados persisten en Kafka. Usa ID nuevo para cada experimento, no resetear grupo para hacer pasar pruebas.
- Si cambia Java, rebuild explícito con ambos Compose. make p03-verify reutiliza contenedores y exige solución final; consulta logs ante timeout.
- make p03-down detiene entorno y conserva volumen. No publicar puertos ni usar credenciales reales.
