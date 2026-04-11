# Proyecto Integrador

## Especificación, Gestión y Planificación

### 1. Requirements

---

#### _Problema que se quiere resolver_

La idea central de este proyecto es desarrollar un ecosistema digital integrado que facilite la gestión administrativa y académica dentro de un entorno institucional. El propósito es plasmar una solución tecnológica robusta que unifique la estructura jerárquica de la organización con el flujo cotidiano de alumnos y docentes.

Para concretar esta visión, el proyecto se enfoca en los siguientes pilares de desarrollo:

- **Integración Institucional**: Se busca modelar una estructura clara que vincule las Unidades Académicas (Facultades) con sus respectivos Materias, permitiendo que el sistema refleje fielmente la organización de la institución.
- **Automatización y Experiencia de Usuario**: La propuesta pone énfasis en simplificar tareas administrativas críticas. Esto incluye la generación automatizada de credenciales seguras para docentes y la gestión dinámica de perfiles, asegurando que cada actor acceda a las herramientas que necesita de forma ágil.
- **Fortalecimiento del Rol Docente**: El sistema busca brindar claridad en la jerarquía de las cátedras, distinguiendo roles entre Jefes de Cátedra y Ayudantes, y proporcionando herramientas directas para el seguimiento del rendimiento académico mediante la carga de calificaciones.
- **Autogestión Estudiantil Inteligente**: Se apunta a desarrollar un entorno donde el alumno tenga el control de su trayectoria. Esto implica desde una inscripción asistida por lógica de correlatividades hasta el acceso en tiempo real a la información logística de cursada, como horarios, aulas y modalidades.
- **Comunicación y Seguridad**: La idea es proyectar una plataforma confiable que garantice la protección de datos sensibles mediante estándares de seguridad modernos (como el hasheo con BCrypt) y que facilite la comunicación institucional a través de notificaciones automáticas por email y sistemas de mensajería integrados.

#### _Usuarios del sistema_

El sistema define tres roles con niveles de acceso diferenciados:

- _Administrador_: Posee permisos totales para la gestión de usuarios y el registro de nuevos profesores.
- _Profesor_: Usuario con acceso al panel de control y a la visualización de su información profesional vinculada.
- _Alumno_: Perfil con acceso básico a las funcionalidades generales de la plataforma.

#### _Funcionalidades principales_

- **Gestión Integral**: Alta, baja, modificación y consulta de las entidades principales: Alumnos, Profesores, Materias y Facultades.
- **Registro Automatizado de Profesores**: Generación de usuario (inicial nombre + apellido) y contraseña temporal (4 últimos dígitos del DNI).
- **Ciclo de Vida de Credenciales y Seguridad**: Implementación de un flujo de seguridad de alta al rol de profesor, donde el Administrador genera una cuenta con datos predeterminados. El sistema gestiona el envío automático de estas credenciales vía email, activando un protocolo de seguridad que obliga al usuario a personalizar su contraseña en el primer ingreso y permite la actualización opcional de su nombre de usuario para mayor comodidad.
- **Gestión de Cursada**:
  - Inscripción de alumnos a materias (teniendo en cuenta correlatividades).
  - Carga de calificaciones por parte de los docentes.
  - Definición de características de la materia: Horarios, aula, modalidad y sistema de mensajería.
- **Jerarquía Académica**: Asignación de Roles específicos dentro de la materia (Jefe de Cátedra y Ayudantes).
- **Dashboard Dinámico**: Adaptación total de la interfaz según el rol; el administrador gestiona la estructura, el profesor carga notas y el alumno se inscribe.

#### _Restricciones técnicas_

- _Persistencia Local_: Uso de una base de datos embebida, lo que facilita la portabilidad pero limita la escalabilidad masiva sin migrar el motor. El uso de una base de datos local requiere un manejo estricto de las claves foráneas (Foreign Keys) para evitar registros huérfanos entre las tablas de usuarios y perfiles específicos.
- _Instrumentación Obligatoria_: Debido a la arquitectura de ActiveJDBC, los cambios en el esquema de la base de datos requieren una recompilación del bytecode para mantener la sincronía.
- _Codificación de Caracteres_: Limitación técnica con caracteres especiales (como la "ñ") en formularios, que requirieron ajustes en el servidor para evitar caídas del sistema.

#### _Tecnologías elegidas y justificación_

- _Backend (Java + Spark)_: Framework ágil y ligero que permite un desarrollo rápido de servicios web.
- _ORM (ActiveJDBC)_: Elegido por su alto rendimiento y por simplificar el mapeo de tablas a objetos Java.
- _Base de Datos (SQLite)_: Proporciona una solución de almacenamiento ligera y sin configuración de servidor externa.
- _Seguridad (BCrypt)_: Implementado para asegurar el hasheo de contraseñas, evitando el almacenamiento de datos sensibles en texto plano.
- _Frontend (Mustache + Tailwind CSS)_: Combinación de un motor de plantillas eficiente con un framework de diseño moderno y responsivo.

#### _Cambios de alcance ocurridos_

- _Evolución de la Persistencia_: El proyecto inició con una planilla de registro estática donde los datos no se guardaban y el usuario no podia realizar acciones tras el login. A un sistema relacional completo con tablas vinculadas para usuarios, perfiles docentes y administrativos.
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
