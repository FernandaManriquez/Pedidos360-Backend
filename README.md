# Pedidos360 Backend

Backend de Pedidos360 desarrollado con Spring Boot y arquitectura de microservicios.

## Arquitectura

El backend está compuesto por tres aplicaciones:

- `bff`: puerto 8080.
- `servicioProductos`: puerto 8081.
- `servicioPedidos`: puerto 8082.

El BFF utiliza OpenFeign para comunicarse con los microservicios y reenvía el encabezado `Authorization` recibido desde el frontend. Los tres servicios validan JWT emitidos por Microsoft Entra ID.

## Tecnologías

- Java 21
- Spring Boot
- Spring Security
- OAuth 2.0 Resource Server
- OpenFeign
- JPA / Hibernate
- MariaDB / MySQL
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

## Seguridad

La autenticación se realiza con Microsoft Entra ID. El frontend obtiene un JWT mediante OAuth 2.0/OpenID Connect y lo envía a AWS API Gateway. API Gateway valida el token antes de reenviar la solicitud al BFF. El BFF y los microservicios también validan el JWT como defensa en profundidad.

## Despliegue

El backend está preparado para ejecutarse en AWS EC2. API Gateway funciona como punto de entrada público y enruta las solicitudes al BFF.
