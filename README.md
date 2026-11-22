# Pedidos360 Backend — EA3 Kafka

Backend Java 21 y Spring Boot. Integra productos y pedidos, Microsoft Entra ID, MariaDB en AWS, RabbitMQ y un pipeline Kafka de alertas.

Rama de entrega: `feature/ea3-kafka`. Estado documentado al 22 de noviembre de 2026. El código y las pruebas locales de esta rama deben distinguirse de los JAR anteriores que actualmente ejecuta EC2.

## Estado verificado

| Componente | Entorno y resultado |
|---|---|
| MariaDB | AWS; utilizada por los servicios y por la integración local mediante túnel SSH. |
| BFF, Productos y Pedidos | Servicios anteriores activos en EC2; consultas de productos y pedidos mediante API Gateway con HTTP 200. |
| Angular | Ejecución local; perfil AWS autenticado contra API Gateway. |
| Kafka EA3 | Docker local: tres ZooKeeper, tres brokers y Kafka UI. |
| RabbitMQ | Docker Compose local: dos nodos, tres colas funcionales y sus tres DLQ; exchanges direct y topic. |
| Administrador Kafka | Implementado y probado localmente con JWT. |
| Frontend alojado en AWS | Bucket S3 creado; intento CloudFront bloqueado por permiso IAM de creación de OAC. |

La mensajería EA3 no está desplegada en EC2. La instancia medida dispone de 913 MiB de RAM y aproximadamente 48 MiB disponibles; el disco conserva 3,7 GB libres. La restricción de capacidad observada corresponde principalmente a memoria. No se presenta este diagnóstico como prueba de una caída por falta de memoria.

## Aplicaciones

| Aplicación | Puerto | Responsabilidad |
|---|---:|---|
| bff | 8080 | Entrada REST y llamadas a los microservicios. |
| servicioProductos | 8081 | Catálogo y persistencia de productos. |
| servicioPedidos | 8082 | Persistencia y publicación de pedidos. |
| servicioNotificaciones | 8083 | Tickets, notificaciones y consumidores Kafka bajo el perfil kafka. |
| servicioRabbitAdmin | 8084 | Administración de colas, exchanges y bindings. |
| servicioKafkaAdmin | 8086 | Administración de tópicos y grupos; escucha localmente en 127.0.0.1. |
| Kafka UI | 8091 | Inspección del clúster Kafka local. |

La segunda instancia de Notificaciones utilizada en la prueba de balanceo escuchó en 8085.

## Pipeline Kafka

1. Pedidos guarda el pedido y publica `PedidoCreadoEvent` en `pedidos360.pedido-creado`.
2. La clave es el identificador del pedido. El evento incluye eventId, pedidoId, usuario, productoId, cantidad, total, estado y fecha.
3. `PedidoKafkaConsumer` valida el evento y delega la regla en `PedidoAlertaService`.
4. Un total superior a 10000 genera una alerta ALTA en `pedidos360.alertas`. Los totales válidos menores o iguales a ese umbral no generan alerta.
5. `AlertaKafkaConsumer` delega la validación en `AlertaAuditoriaService` y registra la recepción. No se implementó una base de datos de auditoría.

| Tópico | Particiones | Réplicas | min.insync.replicas | retention.ms | cleanup.policy |
|---|---:|---:|---:|---:|---|
| pedidos360.pedido-creado | 3 | 3 | 2 | 604800000 (7 días) | delete |
| pedidos360.alertas | 3 | 3 | 2 | 2419200000 (28 días) | delete |

La retención del evento de pedido permite una ventana operativa de reprocesamiento. La alerta conserva una ventana mayor para revisión. Las réplicas protegen frente a fallos de brokers; los tres brokers ejecutados en un único equipo no protegen frente a la pérdida de ese equipo.

Las propiedades de los brokers no sustituyen las propiedades específicas de retención de cada tópico. La creación explícita puede realizarse con la API de administración. La creación automática está deshabilitada.

Grupos independientes:
- `pedidos360-alertas-ea3`: evaluación de pedidos.
- `pedidos360-auditoria-ea3`: recepción y registro de alertas.

Dentro de un mismo grupo las particiones se reparten entre instancias. Los grupos de flujos diferentes mantienen offsets independientes.

### Productores y errores

Los productores utilizan clave String, JSON tipado, acks=all e idempotencia habilitada.
Los consumidores utilizan ErrorHandlingDeserializer, confirmacion por registro y auto-commit desactivado.

El manejador configura tres reintentos adicionales separados por un segundo para errores recuperables.
Los errores de validacion y deserializacion pasan directamente a una DLT.
Se agregan pedidos360.pedido-creado.DLT y pedidos360.alertas.DLT, configuradas con tres particiones,
tres replicas, minISR2, retencion de 28 dias y politica delete.

La recuperacion exige confirmacion del envio a la DLT antes de permitir el avance del offset.
Si ese envio falla, el registro no se considera recuperado.
La DLT conserva el rechazo para revision; no implementa reprocesamiento automatico.

Se verificaron un pedido con total negativo, JSON malformado y una alerta invalida en las DLT.
Los eventos validos posteriores se procesaron sin detener los consumidores.
El agotamiento de reintentos por fallo transitorio sigue sin una prueba registrada.
Configuracion y resultados: [Kafka DLT](docs/KAFKA_DLT.md).

## RabbitMQ

Se conserva el flujo de tickets y notificaciones de la etapa anterior.

| Elemento | Nombre |
|---|---|
| Exchange | pedidos360.eventos |
| Routing key | pedido.creado |
| Colas | pedidos360.tickets; pedidos360.notificaciones |
| DLX | pedidos360.dlx |
| DLQ | pedidos360.tickets.dlq; pedidos360.notificaciones.dlq |

Los archivos se generan en `servicioNotificaciones/output/tickets/<eventId>.txt` y `output/notificaciones/<eventId>.txt`. El pedido 9 produjo ambos archivos con datos coherentes. El 22 de noviembre se verificaron dos nodos en Compose, tres colas funcionales y sus tres DLQ. Auditoria genera un tercer archivo por evento mediante un exchange topic. Configuracion y pruebas en docs/RABBITMQ_CLUSTER.md.

## Administración Kafka

Todas las solicitudes requieren JWT. La API actual no distingue un rol administrativo específico.

| Método | Ruta | Operación |
|---|---|---|
| GET | /api/kafka/topics | Listar tópicos del prefijo del proyecto. |
| GET | /api/kafka/topics/{nombre} | Consultar particiones, réplicas, ISR y configuración. |
| POST | /api/kafka/topics | Crear tópico con configuración inicial. |
| DELETE | /api/kafka/topics/{nombre} | Eliminar tópico. |
| GET | /api/kafka/groups | Listar grupos del proyecto. |
| GET | /api/kafka/groups/{grupo} | Consultar estado, miembros, asignaciones y offsets confirmados. |
| DELETE | /api/kafka/groups/{grupo} | Eliminar un grupo sin miembros activos. |

La API permite actualizar retencion y politica de limpieza mediante PATCH /api/kafka/topics/{nombre}/config. Operacion y pruebas documentadas en docs/KAFKA_CONFIG.md. El lag se inspeccionó mediante CLI; la consulta de grupos de la API no calcula el lag.

Se verificaron: 401 sin JWT; consulta 200; creación 201; eliminación 204; tópico inexistente 404; entrada inválida 400; eliminación de grupo activo 409; eliminación de grupo de prueba inactivo 204.

Ejemplo de cuerpo para crear un tópico temporal:

```json
{
  "nombre": "pedidos360.prueba-admin",
  "particiones": 3,
  "replicas": 3,
  "retencionMs": 604800000,
  "politicaLimpieza": "delete"
}
```

Eliminar únicamente los recursos de prueba identificados. No eliminar los tópicos funcionales para repetir la demostración.

## Ejecución local

Requisitos: Java 21, Docker Compose, MariaDB accesible, configuración de Entra ID y credenciales fuera del repositorio.

Desde la raíz del backend, en PowerShell:

```powershell
docker compose -f compose.kafka.yaml up -d zk1 zk2 zk3
docker compose -f compose.kafka.yaml up -d broker1 broker2 broker3 kafka-ui
docker compose -f compose.kafka.yaml ps
```

`depends_on` establece orden, pero este Compose no implementa comprobaciones de readiness. Comprobar que ZooKeeper y los brokers están operativos antes de iniciar los clientes; si un broker termina durante el arranque, revisar sus logs antes de reintentarlo. Los volúmenes contienen mensajes y offsets: no usar `down -v` para un reinicio normal.

Configurar las variables requeridas por cada aplicación:
- `AZURE_ISSUER_URI`: issuer del tenant Entra ID.
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` en Productos y Pedidos.
- En las pruebas con túnel SSH: `DB_URL=jdbc:mysql://127.0.0.1:3307/pedidos360`.
- BFF: `PEDIDOS_SERVICE_URL=http://localhost:8082`, `PRODUCTOS_SERVICE_URL=http://localhost:8081`, `CORS_ALLOWED_ORIGIN=http://localhost:4200`.

Los archivos `application-kafka.properties` contienen la conexión a localhost:19092, localhost:29092 y localhost:39092. Activar el perfil `kafka` en Pedidos y Notificaciones. Desde la carpeta de cada uno:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=kafka"
```

Desde `servicioKafkaAdmin`:

```powershell
.\mvnw.cmd spring-boot:run
```

El perfil Kafka complementa RabbitMQ; se requiere también la configuración RabbitMQ del proyecto. No debe ejecutarse Pedidos suponiendo que Kafka reemplaza automáticamente las dependencias RabbitMQ.

### Inspección de los grupos

```powershell
docker compose -f compose.kafka.yaml exec -T broker1 kafka-consumer-groups --bootstrap-server broker1:9092 --describe --group pedidos360-alertas-ea3
docker compose -f compose.kafka.yaml exec -T broker1 kafka-consumer-groups --bootstrap-server broker1:9092 --describe --group pedidos360-auditoria-ea3
```

El lag es la diferencia entre el final de cada partición y el offset confirmado por el grupo. Un lag cero acredita que el grupo alcanzó ese final en la medición; no demuestra por sí solo la corrección de todos los efectos de negocio.

## Evidencias de funcionamiento

- Dos tópicos con P3, RF3 e ISR completos durante la inspección.
- Procesamiento válido sin alerta (total 500) y con alerta ALTA (total 15000).
- Dos instancias de consumidores: reparto 2+1 de particiones; al detener una, la restante recuperó las tres.
- Registro inválido con total -1: detención del listener y lag 1.
- Pedido real 9: producto 3, cantidad 1, total 45990, estado PENDIENTE. Evento `9208cb6c-b90e-4dd6-bf2c-96f246871ad3`; publicación de pedido y alerta en partición 2, offset 1.
- Ticket y notificación RabbitMQ del mismo pedido.
- Frontend local contra API Gateway: productos y pedidos HTTP 200. Petición sin JWT HTTP 401 y preflight CORS HTTP 204.

## Alcance y limitaciones

El guardado en base de datos y las publicaciones RabbitMQ/Kafka son operaciones separadas. No hay outbox ni garantía de entrega exactamente una vez de extremo a extremo. Una publicación fallida puede dejar un pedido persistido sin su evento. La idempotencia del productor no evita todas las duplicaciones de negocio al reprocesar.

Las pruebas de balanceo corresponden a consumidores y apagado controlado. No constituyen una prueba de caída abrupta de un broker ni de recuperación de un equipo.

El backend que ejecuta EC2 conserva artefactos anteriores; no se ha demostrado allí la ejecución de los componentes Kafka EA3. El bucket S3 fue creado, pero la publicación privada mediante CloudFront falló por `cloudfront:CreateOriginAccessControl`. No se afirma que toda alternativa de alojamiento esté prohibida.

Las compilaciones realizadas con `-DskipTests` no acreditan ejecución de pruebas automatizadas. La matriz de revisión y las brechas se detallan en [docs/REVISION_EA3.md](docs/REVISION_EA3.md).

Documentación histórica: [README_EP3.md](README_EP3.md) y [README_AWS_ETAPA_ANTERIOR.md](README_AWS_ETAPA_ANTERIOR.md). Sus resultados deben leerse con el contexto de esa etapa.
