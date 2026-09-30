# Contrato REST — S3 (Agendamiento de Citas)

Base local: `http://localhost:8080`. JSON camelCase. Errores: `{ "code": "INVALID|DUPLICATE|UNAUTHORIZED|FORBIDDEN|NOT_FOUND|CONFLICT", "message": "texto" }`.

## 1. Catálogos (Públicos)
| Método | Ruta | Headers | Descripción |
|---|---|---|---|
| GET | `/api/catalogs/locations` | - | Lista sedes fijas (HIC, ICV) |
| GET | `/api/catalogs/specialties` | - | Lista especialidades (duraciones 30/60m, general vs especializada) |
| GET | `/api/catalogs/appointment-statuses` | - | Lista estados de citas (REQUESTED, APPROVED, REJECTED, CANCELLED, COMPLETED, NO_SHOW) |

## 2. Profesionales y Bloques
| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| GET | `/api/professionals` | USER | Lista profesionales (filtros: active, specialtyId, locationId) |
| GET | `/api/professionals/{id}` | USER | Detalle del profesional con sedes y especialidades |
| GET | `/api/professionals/{id}/blocks` | USER | Bloques de disponibilidad del profesional |
| POST | `/api/professionals/{id}/blocks` | PROFESSIONAL / ADMIN | Crea bloque de disponibilidad (discretiza en slots de 30m, RN-06 no pasado, RN-07 sede asignada) |
| DELETE | `/api/professionals/{id}/blocks/{blockId}` | PROFESSIONAL / ADMIN | Elimina bloque si no tiene citas asignadas |

## 3. Disponibilidad de Horarios
| Método | Ruta | Query Params | Descripción |
|---|---|---|---|
| GET | `/api/availability` | `locationId`, `specialtyId` (req), `professionalId`, `date` (req, ISO) | Consulta franjas libres (si especialidad es 60 min, retorna pares consecutivos de slots libres, RN-05) |

## 4. Citas — Paciente (USER)
| Método | Ruta | Body | Éxito |
|---|---|---|---|
| POST | `/api/appointments` | `{ professionalId, locationId, specialtyId, startAt }` | 201 Appointment (General: `APPROVED` automático [RN-02]; Especializada: `REQUESTED` [RN-03]; Slot retenido [RN-01]) |
| GET | `/api/appointments/my-appointments` | - | 200 List\<Appointment\> ordenadas por fecha |
| PATCH | `/api/appointments/{id}/cancel` | - | 200 Appointment en estado `CANCELLED` y slots liberados (RN-09, RF-14) |
| GET | `/api/appointments/{id}/history` | - | 200 List\<AppointmentStatusHistory\> (auditoría RF-19) |

## 5. Citas — Bandeja Administrativa (ADMIN)
| Método | Ruta | Query / Body | Descripción |
|---|---|---|---|
| GET | `/api/admin/appointments` | `statusId`, `locationId`, `professionalId`, `date` | Listado con filtros para gestión administrativa (RF-18) |
| PATCH | `/api/admin/appointments/{id}/approve` | - | Aprueba cita en `REQUESTED` pasando a `APPROVED` |
| PATCH | `/api/admin/appointments/{id}/reject` | `{ "reason": "texto" }` | Rechaza cita exigiendo motivo (RN-04) y libera slots (RN-09) |

## 6. Citas — Agenda del Profesional (PROFESSIONAL)
| Método | Ruta | Query / Body | Descripción |
|---|---|---|---|
| GET | `/api/professional/appointments` | `date` (ISO opcional) | Citas `APPROVED` asignadas al médico logueado (RF-16) |
| PATCH | `/api/professional/appointments/{id}/complete` | - | Marca cita como `COMPLETED` (RF-17) |
| PATCH | `/api/professional/appointments/{id}/no-show` | `{ "reason": "opcional" }` | Marca cita como `NO_SHOW` (RF-17) |
