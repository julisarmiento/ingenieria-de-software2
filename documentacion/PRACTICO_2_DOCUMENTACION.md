# Práctico 2B: Documentación y Análisis de Diseño

Este repositorio contiene el trabajo práctico 2B, fork del proyecto base de la cátedra.

**Grupo:**
* Agostina Cruceño
* Ana Luz Masoero
* Leonardo Campos
* Yaideem Testa
* Tomas Monzon
* Manuel Barbieri Pariani

---

## Análisis del Código Base y Patrones de Diseño

A continuación, se documenta el análisis solicitado sobre el patrón de diseño identificado en el código base proporcionado por la cátedra.

### 1. ¿Qué patrón identificaron?

Se necesita un único punto de acceso centralizado a la información académica de la universidad, que permita consultar y actualizar los datos dentro de una misma instancia de gestión.
El patrón Singleton garantiza que exista una sola instancia de la clase encargada de controlar el acceso a los datos compartidos, asegurando que todos los usuarios trabajen sobre la misma fuente de información y se mantenga la coherencia en el sistema.

### 2. ¿Dónde y cómo se aplica en el código?

Se ve reflejado principalmente en la clase DBConfigSingleton, ya que posee el constructor privado "DBConfigSingleton()" y ademas tambien el getInstance() de tipo DBConfigSingleton (la misma clase), donde corrobora que la instancia esté creada o la crea, en caso contrario, retorna dicha instanciaa
Este patrón se puede observar principalmente en las siguientes clases y paquetes:
* ⁠⁠ src/main/java/com/paquete/is1/proyecto/config/DBConfigSingleton ⁠

Luego, App usa esta instancia para abrir y cerrar la conexión de ActiveJDBC (Base.open y Base.close) dentro de los filtros before y after de Spark, asegurando que todos los procesos utilicen la misma configuración.

### 3. ¿Qué problema resuelve este patrón en ese contexto?

En el contexto específico del sistema de gestión estudiantil (particularmente en el login y gestión de usuarios), este patrón resuelve el problema de garantizar una unica fuente de configuración: la aplicación solo necesita una configuración de base de datos (una URL, un driver, un usuario). Si permitieras crear múltiples instancias (new DBConfigSingleton()), podrías tener accidentalmente diferentes partes de tu aplicación intentando conectarse a bases de datos distintas o con credenciales diferentes. El Singleton fuerza a que solo exista un objeto de configuración.