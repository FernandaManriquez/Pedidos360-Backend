# Pedidos360 Backend

Backend de Pedidos360 desarrollado con Spring Boot y arquitectura de microservicios.

## Arquitectura

El backend está compuesto por tres aplicaciones:

- `bff`: puerto 8080.
- `servicioProductos`: puerto 8081.
- `servicioPedidos`: puerto 8082.

El BFF utiliza OpenFeign para comunicarse con los microservicios y reenvía el encabezado `Authorization` recibido desde el frontend. Los tres servicios validan JWT emitidos por Microsoft Entra ID.

El flujo implementado es:

`Frontend Angular → AWS API Gateway → BFF → Microservicios → MariaDB`

## Tecnologías

- Java 21
- Spring Boot
- Spring Security
- OAuth 2.0 Resource Server
- OpenFeign
- JPA / Hibernate
- MariaDB
- Microsoft Entra ID
- AWS EC2
- AWS API Gateway

## Endpoints

### Productos

- `GET /api/productos`
- `POST /api/productos`
- `GET /api/productos/{id}`
- `PUT /api/productos/{id}`
- `DELETE /api/productos/{id}`

### Pedidos

- `GET /api/pedidos`
- `POST /api/pedidos`
- `GET /api/pedidos/{id}`
- `GET /api/pedidos/usuario/{usuario}`
- `PUT /api/pedidos/{id}/estado`
- `DELETE /api/pedidos/{id}`

## Variables de entorno

Los servicios utilizan variables de entorno para evitar guardar credenciales en el repositorio.

### Todos los servicios

```text
AZURE_ISSUER_URI=https://login.microsoftonline.com/<TENANT_ID>/v2.0
```

### Servicio de Productos y Servicio de Pedidos

```text
DB_URL=jdbc:mysql://localhost:3306/pedidos360
DB_USERNAME=<usuario>
DB_PASSWORD=<contraseña>
```

### BFF

```text
PEDIDOS_SERVICE_URL=http://localhost:8082
PRODUCTOS_SERVICE_URL=http://localhost:8081
CORS_ALLOWED_ORIGIN=http://localhost:4200
```

## Ejecución local

Iniciar cada aplicación desde su carpeta:

```bash
./mvnw spring-boot:run
```

Orden recomendado:

1. `servicioProductos`
2. `servicioPedidos`
3. `bff`

## Seguridad y autenticación

La autenticación se realiza con Microsoft Entra ID. El frontend obtiene un JWT mediante OAuth 2.0/OpenID Connect y lo envía a AWS API Gateway. API Gateway valida el token antes de reenviar la solicitud al BFF. El BFF y los microservicios también validan el JWT como defensa en profundidad.

## Despliegue e integración en AWS

El backend fue desplegado en una instancia AWS EC2. Los tres servicios se ejecutan mediante `systemd` y AWS API Gateway funciona como punto de entrada público hacia el BFF.

A continuación se presentan las evidencias de configuración y funcionamiento.

### Rutas configuradas en API Gateway

AWS API Gateway fue utilizado como API Manager del sistema. Se configuraron las rutas necesarias para productos y pedidos, utilizando los métodos GET, POST, PUT y DELETE.

![Rutas configuradas en API Gateway] <img width="2048" height="1030" alt="api-gateway-rutas" src="https://github.com/user-attachments/assets/4c8332b0-90d1-4484-8c09-b5bd7b558c67" />


### Integración con el BFF desplegado en EC2

Las rutas del API Gateway se encuentran asociadas a una integración HTTP que dirige las solicitudes hacia el BFF desplegado en la instancia EC2.

![Integración API Gateway con BFF] <img width="2048" height="1064" alt="api-gateway-integracion" src="https://github.com/user-attachments/assets/7bc6c43b-9923-4dca-81f0-dfe974701ed9" />


### Protección mediante JWT

Se configuró el autorizador `Pedidos360-JWT` en API Gateway. El token es recibido mediante el encabezado `Authorization` y se valida utilizando Microsoft Entra ID como proveedor de identidad.

![Autorizador JWT] <img width="2048" height="902" alt="api-gateway-jwt" src="https://github.com/user-attachments/assets/372570fb-4b0e-40b9-a069-67c1df9f8ce6" />


### Prueba de acceso sin token

Al realizar una solicitud directamente al API Gateway sin enviar un JWT válido, el servicio responde con `HTTP 401 Unauthorized`, demostrando que las rutas protegidas no permiten acceso anónimo.

![Respuesta 401 Unauthorized] <img width="2048" height="660" alt="jwt-401-unauthorized" src="https://github.com/user-attachments/assets/1f4ab5dd-5152-4fa6-9fd6-6d4a5a1642ce" />


### Servicios activos en EC2

El BFF y los dos microservicios fueron configurados como servicios del sistema en la instancia EC2.

![Servicios activos en EC2] <img width="2048" height="705" alt="ec2-servicios-activos" src="https://github.com/user-attachments/assets/59ab8956-8402-48d5-a765-5b30080c8456" />


Servicios verificados:

- `pedidos360-bff`
- `pedidos360-productos`
- `pedidos360-pedidos`

Los tres servicios se encuentran activos y permiten que API Gateway se comunique correctamente con el backend.

