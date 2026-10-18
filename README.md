# Pedidos360 Backend — EP3

Backend desarrollado con Java 21 y Spring Boot para gestionar productos y pedidos, con autenticación mediante Microsoft Entra ID y procesamiento de eventos con RabbitMQ.

## Estado de la entrega

Se verificó el funcionamiento del frontend Angular y del backend en ejecución local, conectados a MariaDB alojada en AWS mediante un túnel SSH.

RabbitMQ se ejecuta localmente en Docker. Su despliegue en AWS quedó pendiente con autorización del profesor, debido a los inconvenientes presentados en EC2.

Las evidencias de la etapa anterior de despliegue están en [README_AWS_ETAPA_ANTERIOR.md](README_AWS_ETAPA_ANTERIOR.md). Corresponden a esa etapa y no representan el estado actual de ejecución.

## Aplicaciones

| Aplicación | Puerto | Función |
|---|---:|---|
| bff | 8080 | Recibe solicitudes del frontend y consulta los microservicios mediante OpenFeign. |
| servicioProductos | 8081 | Gestiona productos y su persistencia. |
| servicioPedidos | 8082 | Gestiona pedidos y publica el evento de creación. |
| servicioNotificaciones | 8083 | Consume eventos para generar tickets y notificaciones internas. |
| servicioRabbitAdmin | 8084 | Administra colas, exchanges y bindings mediante una API REST. |

El frontend Angular se ejecuta en el puerto 4200. Las aplicaciones del backend validan JWT emitidos por Microsoft Entra ID.

## Flujo verificado

1. El usuario inicia sesión en Angular mediante Microsoft Entra ID.
2. Angular envía el JWT al BFF, que reenvía Authorization a los microservicios.
3. Productos y Pedidos acceden a MariaDB en AWS.
4. Al crear un pedido, Pedidos guarda sus datos y publica un evento en RabbitMQ.
5. El exchange entrega el evento a las colas de tickets y notificaciones.
6. Cada consumidor genera su archivo y envía un ACK al terminar correctamente.

## RabbitMQ

La configuración declara beans de Queue, Exchange y Binding, con nombres centralizados mediante propiedades y separación entre configuración y lógica de negocio.

| Elemento | Nombre |
|---|---|
| Exchange de eventos | pedidos360.eventos |
| Routing key de creación | pedido.creado |
| Cola de tickets | pedidos360.tickets |
| Cola de notificaciones | pedidos360.notificaciones |
| Exchange de mensajes rechazados | pedidos360.dlx |
| DLQ de tickets | pedidos360.tickets.dlq |
| DLQ de notificaciones | pedidos360.notificaciones.dlq |

Los consumidores utilizan @RabbitListener y confirmación manual. Los eventos inválidos se rechazan sin reencolarlos y se dirigen a su DLQ. Los errores de escritura tienen intentos limitados; si se agotan, el mensaje también se envía a la DLQ.

Los archivos se generan dentro de servicioNotificaciones:

- output/tickets/<eventId>.txt
- output/notificaciones/<eventId>.txt

## API REST

Los endpoints requieren un JWT válido.

### Productos

- GET /api/productos
- POST /api/productos
- GET /api/productos/{id}
- PUT /api/productos/{id}
- DELETE /api/productos/{id}

### Pedidos

- GET /api/pedidos
- POST /api/pedidos
- GET /api/pedidos/{id}
- GET /api/pedidos/usuario/{usuario}
- PUT /api/pedidos/{id}/estado
- DELETE /api/pedidos/{id}

La creación valida usuario obligatorio, producto positivo, cantidad mayor que cero y total no negativo.

### Administración de RabbitMQ — puerto 8084

- POST /api/rabbit/queues
- GET /api/rabbit/queues/{name}
- DELETE /api/rabbit/queues/{name}
- POST /api/rabbit/exchanges
- DELETE /api/rabbit/exchanges/{name}
- POST /api/rabbit/bindings
- DELETE /api/rabbit/bindings

El controlador delega las operaciones en RabbitAdminService. Se validan los datos de entrada antes de administrar los recursos.

## Configuración y ejecución local

Requisitos: Java 21, Docker con RabbitMQ y acceso a MariaDB.

Configurar AZURE_ISSUER_URI en cada aplicación:

    AZURE_ISSUER_URI=https://login.microsoftonline.com/<TENANT_ID>/v2.0

Productos y Pedidos requieren además:

    DB_URL=jdbc:mysql://127.0.0.1:3307/pedidos360
    DB_USERNAME=<usuario>
    DB_PASSWORD=<contraseña>

La URL anterior corresponde al túnel SSH utilizado en las pruebas: puerto local 3307 hacia MariaDB en EC2, puerto 3306.

El BFF utiliza las siguientes direcciones locales:

    PEDIDOS_SERVICE_URL=http://localhost:8082
    PRODUCTOS_SERVICE_URL=http://localhost:8081
    CORS_ALLOWED_ORIGIN=http://localhost:4200

RabbitMQ utiliza localhost:5672. Su panel de administración está en http://localhost:15672.

Con RabbitMQ y el túnel SSH activos, iniciar cada aplicación en una terminal independiente desde su propia carpeta:

    ./mvnw spring-boot:run

Orden recomendado: servicioProductos, servicioNotificaciones, servicioPedidos, servicioRabbitAdmin y bff.

El frontend debe apuntar a http://localhost:8080 y ejecutarse desde su repositorio mediante npm start.

## Pruebas realizadas

- Creación de un pedido desde Angular.
- Generación del ticket y de la notificación interna con el mismo eventId.
- Confirmaciones ACK de ambos consumidores visibles en los registros.
- Envío de mensajes inválidos a las DLQ.
- Creación, consulta y eliminación de recursos de prueba mediante la API de administración.
- Respuestas 400 ante nombre de cola vacío y tipo de exchange inválido.
- Respuesta 400 al intentar crear un pedido con cantidad cero.
- Compilación y empaquetado de servicioPedidos con ./mvnw -DskipTests package. Este comando no ejecuta pruebas automatizadas.

## Alcance y pendientes

El despliegue de RabbitMQ en AWS permanece pendiente con autorización del profesor.

El guardado del pedido y la publicación del evento son operaciones separadas; no se implementó un mecanismo outbox de recuperación.

La validación del total comprueba que no sea negativo; no recalcula el precio desde el catálogo.

Las contraseñas, tokens y claves privadas deben configurarse fuera del repositorio.

Más detalles de la integración en [README_EP3.md](README_EP3.md).
