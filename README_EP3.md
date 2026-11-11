# Pedidos360 - EP3 RabbitMQ

## Servicios
BFF: 8080. Productos: 8081. Pedidos: 8082.
Notificaciones: 8083. Rabbit Admin: 8084. Angular: 4200.
RabbitMQ: 5672. Consola: http://localhost:15672.

## Flujo verificado
Angular envia pedidos al BFF con JWT de Microsoft Entra ID.
Pedidos guarda en la base de datos y publica pedido.creado.
Exchange directo: pedidos360.eventos.
Colas: pedidos360.tickets y pedidos360.notificaciones.
Cada consumidor genera su documento y despues envia ACK.

## Errores y documentos
Datos invalidos: rechazo sin reencolar hacia DLQ.
IOException: hasta 3 intentos totales por entrega.
Intentos agotados: NACK sin reencolar hacia DLQ.
Exchange de errores: pedidos360.dlx.
DLQ: pedidos360.tickets.dlq y pedidos360.notificaciones.dlq.
Documentos: servicioNotificaciones/output/tickets y output/notificaciones.
El UUID del evento identifica el archivo; repetirlo reemplaza ese archivo.
El publicador registra confirmaciones del broker y mensajes sin ruta.

## Requisitos e inicio
Java 21, Docker Desktop, Git Bash y acceso a la base de datos.
Iniciar RabbitMQ: docker start rabbitmq.
Configurar AZURE_ISSUER_URI, DB_URL, DB_USERNAME y DB_PASSWORD.
Si la base esta en EC2, establecer primero el tunel SSH.
Para el tunel local, DB_URL=jdbc:mysql://127.0.0.1:3307/pedidos360.
Desde la carpeta de cada servicio: ./mvnw spring-boot:run.
Iniciar Pedidos, Productos, Notificaciones, Rabbit Admin y BFF.
Las credenciales se proporcionan por separado y no se guardan en Git.

## API Rabbit Admin
Base: http://localhost:8084/api/rabbit.
Requiere Authorization: Bearer TOKEN y Content-Type: application/json.
- POST /queues: crear cola (name, durable, autoDelete).
- GET /queues/{name}: consultar mensajes y consumidores.
- DELETE /queues/{name}: eliminar cola vacia y sin consumidores.
- POST /exchanges: crear exchange (name, type, durable, autoDelete).
- DELETE /exchanges/{name}: eliminar exchange.
- POST /bindings: crear binding (queue, exchange, routingKey).
- DELETE /bindings: eliminar binding con el mismo cuerpo JSON.
Tipos admitidos: direct, topic y fanout. Se rechaza el prefijo amq.
Respuestas: 200/201/204; 400 datos invalidos; 404 inexistente; 409 conflicto; 503 conexion AMQP.

## Validacion y limites
Se verificaron: crear pedido desde Angular, guardar en BD, generar ambos documentos, publisher ACK y ACK de ambos consumidores.
Se probaron operaciones REST de administracion, nombre vacio (400), cola inexistente (404) y datos invalidos hacia ambas DLQ.
Los reintentos por IOException se revisaron en codigo; no se simulo fallo de disco.
Guardar y publicar son operaciones separadas: no hay outbox ni recuperacion automatica de eventos.
La demostracion usa servicios locales y BD en EC2 mediante SSH.
El acceso anterior por API Gateway presentaba 503 y sigue pendiente.
Compilacion por servicio: ./mvnw -DskipTests package (omite pruebas).
