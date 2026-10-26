# Auto Care 360 – API REST

API REST de la **Plataforma Web de Gestión de Citas y Mantenimiento Vehicular** para el taller **Auto Care 360**.
Automatiza el agendamiento de citas, valida en tiempo real la disponibilidad de bahías y evita solapamientos de turnos.

> Curso Integrador I: Sistemas Software — Jean  Horna (2026)
> El frontend (Angular) consumirá esta API; no forma parte de este repositorio.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Validation, Security) |
| Base de datos | Microsoft SQL Server (H2 en memoria para tests) |
| Persistencia | JPA / Hibernate |
| Seguridad | Spring Security + JWT (jjwt) + BCrypt |
| Librerías | Google Guava, Apache Commons Lang, Apache POI, Logback, Lombok |
| Documentación | springdoc-openapi (Swagger UI) |
| Pruebas | JUnit 5, Mockito, AssertJ, Spring MockMvc |

## Arquitectura y principios

```
Cliente (Angular) ──HTTP/JSON──► Controller ──► Service ──► DAO ──► SQL Server
                                  (REST)      (negocio)   (JPA)
```

- **MVC**: controladores REST (`controller`) separados del modelo (`model`) y de la lógica de negocio (`service`). Las "vistas" son DTOs JSON (`dto`).
- **DAO**: acceso a datos encapsulado en interfaces `*Dao` (Spring Data JPA) con consultas JPQL propias.
- **SOLID**:
  - *S*: cada clase tiene una responsabilidad (p. ej. `MotorReservas` solo valida horarios y solapamientos).
  - *O*: catálogo de errores `CodigoError` extensible sin modificar el manejador global.
  - *L / I*: servicios definidos como interfaces pequeñas y específicas.
  - *D*: los controladores dependen de interfaces (`CitaService`), no de implementaciones.
- **TDD**: cada capa se construyó escribiendo primero los tests (rojo) y luego la implementación (verde).

### Estructura de paquetes

```
com.autocare360.api
├── config       Configuración (seguridad, reloj, propiedades, OpenAPI, datos iniciales)
├── controller   Endpoints REST
├── dao          Acceso a datos (Spring Data JPA)
├── dto          DTOs de entrada (request) y salida (response)
├── exception    Catálogo de errores y manejador global
├── mapper       Conversión Entidad <-> DTO
├── model        Entidades JPA y enums
├── report       Reportes Excel (Apache POI)
├── security     JWT, filtro de autenticación, usuario autenticado
├── service      Interfaces de negocio + MotorReservas
│   └── impl     Implementaciones
└── util         Utilidades (placas, códigos de cita)
```

## Modelo de datos

Tablas: `usuario`, `vehiculo`, `servicio`, `bahia`, `cita` (según el MER del proyecto).
Adiciones respecto al MER:

- `cita.codigo_cita`: código único de la constancia digital (ej. `AC-20261120-X7K2`).
- `vehiculo.activo`: baja lógica que conserva el historial de citas.

Scripts en `database/`:

1. `01_schema.sql`: crea la base `AutoCare360`, tablas, FKs, CHECKs e índices (incluye índice único filtrado contra reservas dobles).
2. `02_data.sql`: catálogo inicial de servicios y bahías.

## Instalación y ejecución

### Requisitos
- JDK 17+
- SQL Server 2019+ con autenticación SQL y TCP/IP habilitado (puerto 1433)
- Git

### Pasos
```bash
git clone https://github.com/jeanhorna/autocare360.git
cd autocare360/autocare360-api
```

1. Ejecutar en SSMS `database/01_schema.sql` y luego `database/02_data.sql`.
2. Configurar credenciales (variables de entorno o `application.properties`):

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de SQL Server | `jdbc:sqlserver://localhost:1433;databaseName=AutoCare360;...` |
| `DB_USER` / `DB_PASSWORD` | Credenciales de SQL Server | `sa` / *(cambiar)* |
| `JWT_SECRET` | Clave Base64 (≥ 32 bytes) para firmar tokens | clave de desarrollo |
| `ADMIN_PASSWORD` | Contraseña del administrador inicial | `Admin123*` |

3. Ejecutar:
```bash
./mvnw spring-boot:run        # Linux / Mac / Git Bash
mvnw.cmd spring-boot:run      # Windows
```

La API queda en `http://localhost:8080`.

### Usuario administrador inicial
Se crea automáticamente al arrancar si no existe:

- **Correo:** `admin@autocare360.pe`
- **Contraseña:** `Admin123*` (cambiar con `ADMIN_PASSWORD`)

## Documentación interactiva (Swagger)

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Para probar endpoints protegidos:
1. Ejecutar `POST /api/auth/login` y copiar el `token`.
2. Pulsar **Authorize** y pegar el token (sin la palabra `Bearer`).

## Endpoints principales

Roles: **CLIENTE**, **RECEPCIONISTA**, **ADMINISTRADOR**. "Personal" = recepcionista o administrador.

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/registro`, `/api/auth/login` | Público |
| GET/PUT | `/api/usuarios/me` | Autenticado |
| GET/POST | `/api/usuarios`, PATCH `/api/usuarios/{id}/rol` | Administrador |
| GET | `/api/servicios`, `/api/servicios/{id}` | Público |
| POST/PUT/DELETE | `/api/servicios[/{id}]` | Administrador |
| GET | `/api/bahias`, `/api/bahias/{id}` | Personal |
| GET | `/api/bahias/operativas` | Autenticado |
| POST/PUT | `/api/bahias[/{id}]` | Administrador |
| PATCH | `/api/bahias/{id}/estado` | Personal |
| GET | `/api/vehiculos/mis-vehiculos` | Autenticado |
| GET | `/api/vehiculos` | Personal |
| POST | `/api/vehiculos` | Autenticado |
| GET/PUT/DELETE | `/api/vehiculos/{placa}` | Dueño o personal |
| GET | `/api/citas/disponibilidad?fecha=&idServicio=` | Autenticado |
| POST | `/api/citas` | Dueño del vehículo o personal |
| GET | `/api/citas/mis-citas` | Autenticado |
| GET | `/api/citas/codigo/{codigo}` | Dueño o personal |
| GET | `/api/citas/panel?fecha=&estado=&idBahia=` | Personal |
| PATCH | `/api/citas/{id}/estado`, `/api/citas/{id}/cancelar` | Personal / dueño |
| GET | `/api/reportes/citas/excel?fecha=&estado=&idBahia=` | Personal |

Ejemplo de reserva (si `idBahia` es `null`, el motor asigna la primera bahía libre):
```json
POST /api/citas
{ "placa": "ABC-123", "idServicio": 1, "idBahia": null, "fechaHoraProgramada": "2026-11-20T09:00:00" }
```

## Reglas del motor de reservas (RF-04)

- Horario de atención configurable (`app.reserva.*`): 08:00–18:00, bloques de 30 min.
- No se reserva en el pasado ni fuera de horario (el servicio debe terminar antes del cierre).
- Una bahía no puede tener citas superpuestas; un vehículo no puede estar en dos bahías a la vez.
- Las bahías en mantenimiento no reciben reservas.
- Concurrencia: bloqueo pesimista de la bahía durante la reserva + índice único filtrado en SQL Server.
- Estados: `PENDIENTE → CONFIRMADA → FINALIZADA`, o `CANCELADA` desde pendiente/confirmada.

## Formato de errores

Todas las respuestas de error tienen el mismo formato:
```json
{
  "timestamp": "2026-11-20T09:00:00",
  "status": 409,
  "codigo": "RES-004",
  "mensaje": "La bahia ya tiene una cita en ese horario",
  "ruta": "/api/citas"
}
```

| Código | HTTP | Significado |
|---|---|---|
| GEN-400 / VAL-001 | 400 | Solicitud mal formada / validación (incluye `errores` por campo) |
| GEN-401 | 401 | No autenticado o credenciales inválidas |
| GEN-403 | 403 | Sin permisos |
| GEN-404 | 404 | Recurso no encontrado |
| USR-001 | 409 | Correo ya registrado |
| VEH-001 / 002 / 003 | 400 / 409 | Placa inválida / duplicada / vehículo con citas activas |
| SRV-001, BAH-001 | 409 | Servicio o bahía duplicados |
| BAH-002 | 409 | Bahía en mantenimiento |
| RES-001 … RES-006 | 400 / 409 | Reglas del motor de reservas |
| CIT-001 | 409 | Cambio de estado de cita no permitido |

## Pruebas

```bash
./mvnw test
```

99 pruebas automatizadas (unitarias con Mockito y de integración con MockMvc + H2), que cubren el motor de reservas, la seguridad JWT, los permisos por rol, el flujo completo del BPMN y la generación del Excel.

## Integración con Angular

- CORS habilitado para `http://localhost:4200` (`app.cors.allowed-origins`).
- Enviar el header `Authorization: Bearer <token>` en cada petición protegida (interceptor HTTP).
- El Excel se descarga como `Blob`; el nombre del archivo viene en el header `Content-Disposition`.

## Logs

Logback (`logback-spring.xml`): consola y archivo con rotación diaria en `logs/autocare360-api.log`.