# Kafka: rechazos y continuidad

Estado verificado localmente el 22 de noviembre de 2026.

## Configuracion

Los dos topicos de negocio conservan sus configuraciones.
Se agregan pedidos360.pedido-creado.DLT y pedidos360.alertas.DLT.
Cada DLT se declara con tres particiones, tres replicas, min.insync.replicas=2,
retencion de 28 dias y cleanup.policy=delete.

Los errores recuperables tienen tres reintentos adicionales separados por un segundo.
Los errores de validacion y deserializacion pasan directamente a recuperacion.
DeadLetterPublishingRecoverer conserva el registro y agrega metadatos del fallo.
La publicacion DLT mantiene la particion de origen.
Se exige confirmacion del envio a DLT antes de considerar recuperado el registro.
Si falla ese envio, el error se propaga y el registro no se considera recuperado.
La DLT conserva los rechazos; no implementa su reprocesamiento automatico.

## Prueba observada

Pedido invalido: 1bd48a6b-ccd0-47f2-b216-8b18ef6e8c4e.
JSON malformado: 290f85c0-c8fa-4208-bd73-bab1d631c7ad.
Ambos se encontraron en pedido-creado.DLT, particion 2, offsets 0 y 1.

Alerta invalida: 12ec3a42-d792-40af-93af-514f4697c618.
Se encontro en alertas.DLT, particion 2, offset 0.

Pedido valido posterior: 5406faa2-1983-46b4-b4d4-1413c47c5770.
Genero alerta recibida en particion 2, offset 2.

Alerta valida posterior: 0a2d6f1f-2338-45b3-ae6a-ac9c4cf6bb7e.
Fue recibida en particion 2, offset 4.

Ambos consumidores siguieron activos. Las particiones con registros mostraron lag cero.
Una particion vacia sin offset confirmado aparecio con guiones.
Los eventos fueron sinteticos; no se crearon pedidos nuevos en MariaDB.

El agotamiento de reintentos por un fallo transitorio no se comprobo en esta prueba.
No se afirma entrega exactamente una vez de extremo a extremo.
Evidencias: docs/evidencias/kafka/dlt-continuidad-*.txt.
