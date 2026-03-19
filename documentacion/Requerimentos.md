# Proyecto Integrador

## Especificación, Gestión y Planificación

### 1. Requirements

---

#### _Problema que se quiere resolver_

El proyecto busca solucionar la falta de una plataforma centralizada para la _gestión administrativa de perfiles docentes y usuarios_ en un entorno institucional. Permite automatizar la creación de credenciales de acceso para profesores basándose en sus datos personales (DNI), garantizando la integridad de la información mediante la vinculación de tablas de usuarios y perfiles específicos.

#### _Usuarios del sistema_

El sistema define tres roles con niveles de acceso diferenciados:

- _Administrador_: Posee permisos totales para la gestión de usuarios y el registro de nuevos profesores.
- _Profesor_: Usuario con acceso al panel de control y a la visualización de su información profesional vinculada.
- _Usuario (Estándar)_: Perfil con acceso básico a las funcionalidades generales de la plataforma.

#### _Funcionalidades principales_

- _Autenticación Segura_: Sistema de inicio de sesión que valida credenciales contra una base de datos.
- _Gestión de Sesiones_: Control de acceso basado en el rol del usuario almacenado en la sesión activa.
- _Registro Automatizado de Profesores_: Generación automática de nombres de usuario (inicial del nombre + apellido) y contraseñas temporales (últimos 4 dígitos del DNI).
- _Dashboard Dinámico_: Interfaz que adapta sus opciones (como el botón "Registrar Profesor") según el rol del usuario logueado.

#### _Restricciones técnicas_

- _Persistencia Local_: Uso de una base de datos embebida, lo que facilita la portabilidad pero limita la escalabilidad masiva sin migrar el motor.
- _Instrumentación Obligatoria_: Debido a la arquitectura de ActiveJDBC, los cambios en el esquema de la base de datos requieren una recompilación del bytecode para mantener la sincronía.
- _Codificación de Caracteres_: Limitación técnica con caracteres especiales (como la "ñ") en formularios, que requirieron ajustes en el servidor para evitar caídas del sistema

#### _Tecnologías elegidas y justificación_

- _Backend (Java + Spark)_: Framework ágil y ligero que permite un desarrollo rápido de servicios web.
- _ORM (ActiveJDBC)_: Elegido por su alto rendimiento y por simplificar el mapeo de tablas a objetos Java.
- _Base de Datos (SQLite)_: Proporciona una solución de almacenamiento ligera y sin configuración de servidor externa.
- _Seguridad (BCrypt)_: Implementado para asegurar el hasheo de contraseñas, evitando el almacenamiento de datos sensibles en texto plano.
- _Frontend (Mustache + Tailwind CSS)_: Combinación de un motor de plantillas eficiente con un framework de diseño moderno y responsivo.

#### _Cambios de alcance ocurridos_

- _Evolución de la Persistencia_: El proyecto inició con una planilla de registro estática donde los datos no se guardaban y el usuario (aun) no puede realizar acciones tras el login.
- _Jerarquía de Datos_: Se expandió el alcance desde una sola página a un sistema semi-completo de gestión con tablas relacionadas para usuarios, profesores y administradores.

#### _Problemas encontrados_

- _Sincronización y Conexión_: Dificultades iniciales para establecer la conexión entre la base de datos SQLite y el servidor de la aplicación Java.
- _Lógica de Inserción Dual_: Errores en App.java causados por líneas duplicadas en la creación de profesores, lo que provocaba que se guardaran solo como usuarios o fallara el proceso por completo.
- _Codificación (El problema de la "ñ")_: Se detectó que el servidor crasheaba al procesar textos con caracteres especiales en los formularios de creación de profesor, obligando a revisar la configuración de caracteres.
- _Sincronización de Modelos_: Conflictos de "Error 500" derivados de la falta de instrumentación tras modificar las tablas de la base de datos.
- _Inconsistencia de Entornos_: Diferencias entre el esquema de desarrollo (dev.db) y producción (prod.db) que afectaron la lectura de roles.

#### _Forma de organización del equipo_

- El desarrollo se organizó bajo una metodología incremental, utilizando Git para el control de versiones y Maven para la gestión de dependencias y procesos de compilación automáticos.
- Se utilizó _Git_ como sistema de control de versiones, implementando un flujo de trabajo basado en _ramas_.
- Se mantuvo una rama de desarrollo (dev) principal y ramas individuales para cada integrante, evitando conflictos de código ("pisarse") y asegurando una integración limpia de las funcionalidades.
