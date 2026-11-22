# Cluster RabbitMQ y auditoria

## Verificacion del 22 de noviembre de 2026

Dos nodos RabbitMQ 4.2.9 activos mediante compose.rabbitmq.yaml.
Tres colas funcionales: tickets, notificaciones y auditoria.
Cada cola tiene consumidor y DLQ independiente.

El exchange direct pedidos360.eventos entrega tickets y notificaciones.
Tambien envia pedido.creado al topic pedidos360.auditoria.eventos.
Este topic enruta a pedidos360.auditoria mediante pedido.*.
El DLX pedidos360.dlx entrega los rechazos a la DLQ correspondiente.

El evento valido e7bfdfb5-2be6-4ccf-ab40-5059c63e7060 genero los tres archivos.
El evento invalido a2488ef0-f55b-4b37-acc4-e312a0a002bd incremento cada DLQ en uno.
Son pruebas sinteticas mediante HTTP API, sin crear pedidos en MariaDB.
Compose conservo el cluster, los consumidores y los mensajes tras recrear los contenedores.

## Arranque del entorno existente

Conservar .env.rabbitmq y los volumenes. Desde la raiz del backend:

    docker compose --env-file .env.rabbitmq -f compose.rabbitmq.yaml up -d --wait --wait-timeout 120
    docker exec rabbitmq rabbitmqctl cluster_status

Iniciar servicioNotificaciones para declarar la topologia y activar los consumidores.
La clave de Erlang se conserva fuera de Git en .env.rabbitmq.

## Instalacion limpia en otro equipo

Solo para un entorno nuevo, sin contenedores del proyecto ni puertos ocupados.
No reemplazar el archivo privado del entorno existente.

Copiar rabbitmq.env.example a .env.rabbitmq.
Reemplazar REEMPLAZAR_CON_CLAVE_ALEATORIA por una clave aleatoria compartida por ambos nodos.
Mantener los nombres de red y volumenes de la plantilla y ejecutar:

    docker network create pedidos360-rabbit
    docker volume create pedidos360-rabbit1-data
    docker volume create pedidos360-rabbit2-data
    docker compose --env-file .env.rabbitmq -f compose.rabbitmq.yaml up -d --wait --wait-timeout 120
    docker exec pedidos360-rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
    docker exec rabbitmq rabbitmqctl cluster_status

La union se ejecuta solo sobre el segundo nodo nuevo y vacio.
Compose inicia los procesos; la union inicial es un paso explicito.
Con volumenes conservados, la pertenencia al cluster persiste.

## Alcance y evidencias

Cluster local en un solo equipo; no acredita ejecucion en AWS.
No se afirma replicacion de las colas classic ni tolerancia probada a perdida de nodo.
Los contenedores before-compose estan detenidos y no deben iniciarse junto a los nuevos: comparten volumenes.
Evidencias en docs/evidencias/rabbitmq/.
