# RabbitMQ: administración y auditoría de DLQ

## Verificación del 28 de noviembre de 2026

Dos nodos RabbitMQ activos en clúster local, sin alarmas ni
particiones de red durante la inspección.

Tres colas funcionales: tickets, notificaciones y auditoría.
Cada una conserva su propia DLQ.

## Administrador RabbitMQ: puerto 8084

Prueba autenticada con recursos temporales:
- Creación de cola, exchange direct y binding: HTTP 201.
- Consulta de la cola: nombre confirmado.
- Eliminación de binding, cola y exchange: HTTP 204.
- Consulta posterior de la cola eliminada: HTTP 404.

## Auditoría de DLQ: puerto 8083

POST /api/rabbit/dlq/{tipo}/auditar

Tipos permitidos: tickets, notificaciones y auditoria.

Cada solicitud obtiene un mensaje con confirmación manual.
Primero guarda un archivo en output/dlq/{tipo}, con metadatos,
cabeceras, contenido UTF-8 y bytes originales codificados en Base64.
Después envía ACK y registra "DLQ registrada" en el log.

Si falla el archivado, intenta devolver el mensaje a la cola.
Si el ACK falla, el archivo puede existir y el mensaje volver a
entregarse; no se garantiza una única copia de auditoría.

La operación retira de la DLQ el mensaje respaldado.
No lo reprocesa ni lo devuelve al flujo de negocio.

Resultados:
- Un mensaje auditado y respaldado desde cada una de las tres DLQ.
- Recepción y contenido registrados en los logs.
- DLQ vacía: HTTP 204.
- Tipo inexistente: HTTP 400.
- Sin token: HTTP 401.
- Token con firma alterada: HTTP 401.

El endpoint utiliza la seguridad JWT existente del servicio.
No se agregó una autorización diferenciada por rol administrativo.

Las pruebas se ejecutaron localmente. Los mensajes auditados
provenían de ensayos anteriores; no se crearon pedidos en MariaDB.

Las evidencias se encuentran en docs/evidencias/rabbitmq.
La compilación de Notificaciones finalizó con BUILD SUCCESS.
