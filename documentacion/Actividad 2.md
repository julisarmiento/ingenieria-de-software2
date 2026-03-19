# Análisis de riesgos por lo alumnos

### Riesgos del proyecto:

- Desacuerdos en la toma de decisiones
- Embarazarse
- Enfermarse
- Falla en algún equipo (compu)
- Falta de conexión
- Atrasos de algún integrante en la materia
- Mala planificación del tiempo
- Fallecimiento de algún integrante
- Falta de comunicación entre los integrantes

### Riesgos del producto:

- Mala conexión del servidor con la base de datos
- Eficiencia temporal baja
- Brechas de seguridad
- Hardcodeo

### Riesgos empresariales o de la organización:

- Cambio de requisitos por parte de los profesores
- Cambio de fecha de entrega

# Análisis de riesgos con IA

# 🛡️ Informe de Auditoría: Análisis de Riesgos del Proyecto

Este documento detalla los riesgos identificados tras la auditoría del código fuente (Java Spark, ActiveJDBC) y la estructura de persistencia (SQLite).

---

## 1. Riesgos Técnicos

_Enfocados en la arquitectura, seguridad y estabilidad del software._

| Riesgo                          | Probabilidad | Impacto     | Descripción                                                                                                                                                |
| :------------------------------ | :----------- | :---------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Falta de Atomicidad (Datos)** | Alta         | **Crítico** | El registro de profesores realiza dos operaciones separadas (`users` y `professors`). Si la segunda falla, queda un usuario huérfano en la DB.             |
| **Seguridad de Roles (RBAC)**   | Media        | Alto        | La validación de permisos depende de comparaciones manuales de texto (ej. `role.equals("admin")`) en cada ruta, lo que aumenta el riesgo de omisión.       |
| **Bloqueo de SQLite**           | Baja         | Medio       | Al ser una base de datos basada en un archivo único, múltiples escrituras simultáneas pueden causar errores de "database is locked".                       |
| **Portabilidad (Hardcoding)**   | Alta         | Medio       | La ruta de la base de datos está fijada de forma absoluta en la configuración del Singleton (`./db/dev.db`), dificultando el despliegue en otros entornos. |

---

## 2. Riesgos Organizacionales

_Factores externos y de gestión que afectan la continuidad._

- **Cambios en los Requisitos Académicos**: Modificaciones en la consigna por parte de los profesores que obliguen a refactorizar la lógica de `App.java`.
- **Gestión de Credenciales**: El uso de contraseñas por defecto visibles en la base de datos de desarrollo (`admin2025`) representa un riesgo si el repositorio es público.
- **Incompatibilidad de Entorno**: Diferencias en las versiones del driver JDBC entre los equipos de los integrantes.

---

## 3. Riesgos de Planificación

_Relacionados con el cumplimiento de las fechas de entrega._

- **Sobrecarga de Frontend**: El mantenimiento manual de múltiples plantillas Mustache (`dashboard`, `login`, `professor`, `user_form`) junto con Tailwind puede consumir más tiempo del previsto.
- **Errores de Integración**: Fallos imprevistos al unir la lógica de sesiones con la persistencia de datos antes de la fecha límite.
- **Validaciones Incompletas**: El riesgo de no llegar a implementar validaciones robustas en los formularios POST, dejando el sistema vulnerable a datos nulos.

---
