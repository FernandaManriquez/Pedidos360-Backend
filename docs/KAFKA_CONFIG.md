# Actualizacion de configuracion Kafka

Verificada localmente el 22 de noviembre de 2026.

Endpoint autenticado:
PATCH /api/kafka/topics/{nombre}/config

Campos opcionales:
- retencionMs: minimo 60000.
- politicaLimpieza: delete, compact o compact,delete.
Debe indicarse al menos un campo.

La operacion usa incrementalAlterConfigs y conserva las propiedades no indicadas.
Reutiliza la validacion del prefijo pedidos360. y comprueba que el topico existe.
La respuesta contiene la configuracion consultada tras completar la actualizacion.
Esta operacion no modifica el numero de particiones ni el factor de replicacion.

## Prueba registrada

Topico temporal: pedidos360.prueba-config-20261122193315.
Creacion: HTTP 201, tres particiones y tres replicas.
Antes: retention.ms=604800000; cleanup.policy=delete.
Actualizacion: HTTP 200.
GET posterior: retention.ms=86400000; cleanup.policy=compact,delete.
Las tres particiones conservaron sus tres replicas e ISR completos.
Retencion negativa: HTTP 400.
Eliminacion del topico temporal: HTTP 204.
Los dos topicos funcionales no fueron modificados.

Evidencia: docs/evidencias/kafka/pedidos360.prueba-config-20261122193315.txt.
La evidencia no contiene el JWT utilizado.
