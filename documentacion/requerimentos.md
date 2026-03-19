# Proyecto Integrador

## Especificación, Gestión y Planificación

### 1. Requirements

---

#### **Problema que se quiere resolver**

El proyecto busca solucionar la falta de una plataforma centralizada para la **gestión administrativa de perfiles docentes y usuarios** en un entorno institucional. Permite automatizar la creación de credenciales de acceso para profesores basándose en sus datos personales (DNI), garantizando la integridad de la información mediante la vinculación de tablas de usuarios y perfiles específicos.

#### **Usuarios del sistema**

El sistema define tres roles con niveles de acceso diferenciados:

- **Administrador**: Posee permisos totales para la gestión de usuarios y el registro de nuevos profesores.
- **Profesor**: Usuario con acceso al panel de control y a la visualización de su información profesional vinculada.
- **Usuario (Estándar)**: Perfil con acceso básico a las funcionalidades generales de la plataforma.

#### **Funcionalidades principales**

- **Autenticación Segura**: Sistema de inicio de sesión que valida credenciales contra una base de datos.
- **Gestión de Sesiones**: Control de acceso basado en el rol del usuario almacenado en la sesión activa.
- **Registro Automatizado de Profesores**: Generación automática de nombres de usuario (inicial del nombre + apellido) y contraseñas temporales (últimos 4 dígitos del DNI).
- **Dashboard Dinámico**: Interfaz que adapta sus opciones (como el botón "Registrar Profesor") según el rol del usuario logueado.

#### **Restricciones técnicas**

- **Persistencia Local**: Uso de una base de datos embebida, lo que facilita la portabilidad pero limita la escalabilidad masiva sin migrar el motor.
- **Instrumentación Obligatoria**: Debido a la arquitectura de ActiveJDBC, los cambios en el esquema de la base de datos requieren una recompilación del bytecode para mantener la sincronía.
- **Codificación de Caracteres**: Limitación técnica con caracteres especiales (como la "ñ") en formularios, que requirieron ajustes en el servidor para evitar caídas del sistema

#### **Tecnologías elegidas y justificación**

- **Backend (Java + Spark)**: Framework ágil y ligero que permite un desarrollo rápido de servicios web.
- **ORM (ActiveJDBC)**: Elegido por su alto rendimiento y por simplificar el mapeo de tablas a objetos Java.
- **Base de Datos (SQLite)**: Proporciona una solución de almacenamiento ligera y sin configuración de servidor externa.
- **Seguridad (BCrypt)**: Implementado para asegurar el hasheo de contraseñas, evitando el almacenamiento de datos sensibles en texto plano.
- **Frontend (Mustache + Tailwind CSS)**: Combinación de un motor de plantillas eficiente con un framework de diseño moderno y responsivo.

#### **Cambios de alcance ocurridos**

- **Evolución de la Persistencia**: El proyecto inició con una planilla de registro estática donde los datos no se guardaban y el usuario (aun) no puede realizar acciones tras el login.
- **Jerarquía de Datos**: Se expandió el alcance desde una sola página a un sistema semi-completo de gestión con tablas relacionadas para usuarios, profesores y administradores.

#### **Problemas encontrados**

- **Sincronización y Conexión**: Dificultades iniciales para establecer la conexión entre la base de datos SQLite y el servidor de la aplicación Java.
- **Lógica de Inserción Dual**: Errores en `App.java` causados por líneas duplicadas en la creación de profesores, lo que provocaba que se guardaran solo como usuarios o fallara el proceso por completo.
- **Codificación (El problema de la "ñ")**: Se detectó que el servidor crasheaba al procesar textos con caracteres especiales en los formularios de creación de profesor, obligando a revisar la configuración de caracteres.
- **Sincronización de Modelos**: Conflictos de "Error 500" derivados de la falta de instrumentación tras modificar las tablas de la base de datos.
- **Inconsistencia de Entornos**: Diferencias entre el esquema de desarrollo (`dev.db`) y producción (`prod.db`) que afectaron la lectura de roles.

#### **Forma de organización del equipo**

- El desarrollo se organizó bajo una metodología incremental, utilizando Git para el control de versiones y Maven para la gestión de dependencias y procesos de compilación automáticos.
- Se utilizó **Git** como sistema de control de versiones, implementando un flujo de trabajo basado en **ramas**.
- Se mantuvo una rama de desarrollo (`dev`) principal y ramas individuales para cada integrante, evitando conflictos de código ("pisarse") y asegurando una integración limpia de las funcionalidades.
