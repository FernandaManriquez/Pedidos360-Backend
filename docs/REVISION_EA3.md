# Revisión de alcance EA3

Fecha de corte: 22 de noviembre de 2026.

Se contrastaron las pautas EP5 (encargo, 12% de la asignatura) y EP6 (presentación, 18%). Los porcentajes siguientes son ponderaciones oficiales por indicador, no una nota asignada al proyecto. Una implementación o configuración no sustituye una evidencia de ejecución.

## Encargo EP5

| Indicador | Ponderación | Estado sustentado y límite |
|---|---:|---|
| Configuración de clientes Kafka | 12% | Bootstrap de tres brokers; serializers/deserializers, grupos, acks e idempotencia configurados; publicación y consumo observados. |
| Productores y consumidores de dos tópicos con DTO | 12% | Pipeline pedido-creado → evaluación → alertas → registro observado. |
| Administrador de componentes Kafka | 6% | Creacion, listado, consulta, actualizacion de retencion y cleanup, y eliminacion implementados. PATCH probado con consulta antes/despues y rechazo de retencion invalida. |
| Productores y serializers | 14% | Productor de pedidos y productor de alertas probados; no hay outbox ni atomicidad con la base de datos. |
| Consumidores y separación de dominio | 14% | Listeners separados de PedidoAlertaService y AlertaAuditoriaService; procesamiento normal observado. |
| Manejo de errores | 12% | Validación y detención por registro inválido probadas. Reintentos configurados; agotamiento por fallo transitorio sin prueba registrada. El listener detenido requiere intervención. |
| Retención, cleanup, particiones y réplicas | 12% | Dos tópicos P3/RF3, minISR2; retenciones 7 y 28 días y política delete inspeccionadas. |
| Consumer groups y distribución resiliente | 12% | Dos instancias con reparto 2+1; tras parada controlada, una asumió las tres particiones. No prueba caída abrupta de broker o equipo. |
| Operaciones repetibles sobre tópicos y grupos | 6% | API de creación/inspección/eliminación y CLI de grupos utilizadas. Actualizacion de retencion y cleanup mediante PATCH probada sobre un topico temporal. |

## Presentación EP6

| Indicador | Ponderación | Material que debe demostrarse |
|---|---:|---|
| Arquitectura tres ZooKeeper y tres brokers | 10% | compose.kafka.yaml, contenedores y función del quórum. Se ejecutó en un solo equipo local. |
| Dos tópicos P3/RF3 | 10% | Configuración, líderes, réplicas e ISR; explicar paralelismo y redundancia. |
| Kafka UI | 5% | Brokers, tópicos, particiones y mensajes en el puerto 8091. |
| Flujo de producción y consumo | 12% | Relacionar evento de pedido, regla total >10000, alerta ALTA y recepción de auditoría. |
| Estructura de eventos | 6% | Explicar eventId, pedidoId como clave, datos de negocio y fecha. |
| Reacción a eventos | 12% | Contrastar caso sin alerta con alerta; pedido real 9 y su correlación. |
| Administración de Kafka | 15% | Particiones, RF3, minISR2, acks, retención, cleanup y grupos. Explicar la actualizacion de retencion y cleanup mediante PATCH y su validacion. |
| Supervisión | 15% | CURRENT-OFFSET, LOG-END-OFFSET, LAG y distribución de miembros. |
| Casos de uso y configuración | 15% | Pedido para evaluación; alerta para revisión; grupos independientes y distinta retención. |

La presentación debe durar entre cinco y diez minutos. Aunque se realiza en grupo, EP6 declara evaluación individual: ambas integrantes deben explicar decisiones y funcionamiento.

## Instrucciones adicionales que no deben omitirse

| Exigencia | Estado al corte |
|---|---|
| Base de datos cloud e integración | Verificada con MariaDB AWS. |
| Login IDaaS y JWT | Entra ID y frontend local; API Gateway 401 sin JWT y 200 con JWT válido. |
| Frontend consumiendo API Manager | Verificado desde localhost hacia API Gateway para productos y pedidos. |
| Frontend y backend desplegados e integrados en cloud | Parcial. Backend anterior activo en EC2; frontend local. S3 creado; CloudFront privado bloqueado por IAM. |
| Dos nodos RabbitMQ en clúster | Verificado localmente: dos nodos activos mediante Docker Compose. |
| Tres colas y sus tres DLQ | Verificadas tres colas funcionales, tres consumidores y tres DLQ; pruebas validas e invalidas registradas. |
| Exchanges direct y topic | Verificados exchanges direct y topic, con bindings funcionales hacia tickets, notificaciones y auditoria. |
| Docker Compose de clúster RabbitMQ | Compose de dos nodos ejecutado localmente; conservo cluster, consumidores y mensajes. |
| Docker Compose de Kafka y Kafka UI | Implementado y ejecutado localmente. |
| Compose RabbitMQ o Kafka en cloud | No ejecutado en el entorno cloud verificado. |
| Administración de actualización de tópicos | Implementada y probada: PATCH autenticado para retencion y cleanup; evidencia antes/despues. |
| Código completo del backend compilado | Se registraron compilaciones de componentes modificados; no se afirma una ejecución nueva y conjunta de todos los módulos. |

## Evidencias y trazabilidad

- Pedido real 9: producto 3, cantidad 1, total 45990, estado PENDIENTE.
- EventId: 9208cb6c-b90e-4dd6-bf2c-96f246871ad3.
- Pedido y alerta: partición 2, offset 1 en sus respectivos tópicos; offsets de ambos grupos avanzaron a 2 en esa partición.
- Ambos archivos RabbitMQ del evento contenían los mismos datos del pedido.
- Caso sintético total 500: válido, sin alerta.
- Caso sintético total 15000: alerta ALTA.
- Caso sintético total -1: listener detenido y lag 1. Su omisión posterior mediante reset de offset fue una intervención sobre datos de ensayo, no un reprocesamiento exitoso del mensaje.
- API Kafka: 200, 201, 204, 400, 401, 404 y 409 según operación documentada.
- API Gateway: consultas productos/pedidos 200; solicitud sin token 401; preflight CORS 204.

## Restricciones y consecuencias

La captura de EC2 mostró 913 MiB de RAM total y aproximadamente 48 MiB disponibles. El disco mostraba 3,7 GB disponibles. Estos valores sustentan un margen de memoria reducido, no una falta de almacenamiento.

CloudFront devolvió denegación de cloudfront:ListDistributions y, al intentar crear la distribución con S3 privado, cloudfront:CreateOriginAccessControl. La creación no concluyó. La evidencia acredita el bloqueo de esas operaciones, no de todo alojamiento cloud posible.

Documentar una limitación no convierte un criterio pendiente en cumplido ni implica una exención docente para EA3. La autorización histórica mencionada en EP3 para RabbitMQ no se extiende automáticamente a esta evaluación.

## Acciones de cierre

1. Mantener separadas las evidencias locales de Kafka EA3 y las consultas al backend anterior en AWS.
2. Incorporar al informe y presentacion la actualizacion Kafka ya verificada mediante PATCH y consulta antes/despues.
3. Actualizar informe y presentacion con las nuevas evidencias RabbitMQ de dos nodos y tres colas con DLQ.
4. Incorporar capturas seleccionadas al informe sin tokens ni contraseñas visibles.
5. Preparar una presentación de cinco a diez minutos con participación de ambas integrantes.
6. Entregar enlaces de la rama feature/ea3-kafka, informe, presentación y video según las instrucciones docentes recibidas.

Esta revisión no certifica cumplimiento total ni asigna una calificación.
