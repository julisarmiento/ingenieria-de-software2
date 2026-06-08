# 🎓 Sistema de Gestión Académica

**Guia para construir, ejecutar y testear el proyecto**

Sistema de gestión académica desarrollado como parte de la materia de Ingeniería de Software II, siguiendo buenas prácticas de diseño, arquitectura MVC + services y calidad de código.

---

## 🛠️ Tecnologías utilizadas

- **Java 21**
- **Spark Framework** — servidor web ligero
- **ActiveJDBC** — ORM para la capa de persistencia
- **SQLite** — base de datos relacional embebida
- **Maven** — gestión de dependencias y build
- **Mustache** — motor de plantillas para las vistas
- **BCrypt** — hash seguro de contraseñas
- **Tailwind CSS** — estilos de la interfaz

---

## ✅ Requisitos previos

- ☕ Java 21
- 📦 Maven instalado
- 💻 Sistema operativo compatible: Windows / Linux / macOS

---

## 🏗️ Construcción del proyecto

Primero, ubicarse en la carpeta del proyecto:

```
cd ingenieria-de-software2
```

Luego, para compilar todo el proyecto y generar los artefactos necesarios ejecuta:

```
mvn clean install
```
Para hacerlo en Windows y MacOs, también funciona:

```
mvn exec:java
```

Lo que hara esto es:

- ✅ Compila clases
- ✅ Ejecuta pruebas
- ✅ Genera artefactos del build

---

## 🎮 Ejecutar el proyecto

Para iniciar el sistema.

Primero hay que dar los permisos necesarios:
```
chmod + x run.sh
```
Luego, ejecutá:

```
./run.sh
```
Si se requiere compilar y ejecutar en un solo paso:

```
./run.sh build
```

Luego abrí tu navegador en:

```
http://localhost:8080
```
---

## 👥 Integrantes del proyecto

- 👨‍💻 [Sarmiento Juliana](https://github.com/julisarmiento)
- 👨‍💻 [Barbieri Manuel](https://github.com/Manuel-Sketch-s)
- 👨‍💻 [Monzon Tomas](https://github.com/tomas-monzon)
- 👨‍💻 [Testa Yaideem](https://github.com/ytesta28)
- 👨‍💻 [Masoero Ana Luz](https://github.com/Ana-Luz-Msr)
