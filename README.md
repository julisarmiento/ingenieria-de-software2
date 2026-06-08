# 🎓 Sistema de Gestión Académica

**Guia para construir, ejecutar y testear el proyecto**

Sistema de gestión académica desarrollado como parte de la materia de Ingeniería de Software II, siguiendo buenas prácticas de diseño, arquitectura MVC y calidad de código.

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
cd sistema-gestion-academica
```

Luego, para compilar todo el proyecto y generar los artefactos necesarios ejecuta:

```
mvn clean install
```

Lo que hara esto es:

- ✅ Compila clases
- ✅ Ejecuta pruebas
- ✅ Genera artefactos del build

---

## 🎮 Ejecutar el proyecto

Para iniciar el sistema ejecuta:

```
./run.sh
```

Si querés compilar y ejecutar en un solo paso:

```
./run.sh build
```

Luego abrí tu navegador en:

```
http://localhost:8080
```

---

## 🧪 Ejecutar los tests

```
mvn test
```

---

## 👥 Integrantes del proyecto

- 👨‍💻 [Juliana Sarmiento](https://github.com/julisarmiento)
- 👨‍💻 [Barbieri Manuel](https://github.com/Manuel-Sketch-s)
- 👨‍💻 [Monzon Tomas](https://github.com/tomas-monzon)
- 👨‍💻 [Testa Yaideem](https://github.com/ytesta28)
- 👨‍💻 [Masoero Ana Luz](https://github.com/Ana-Luz-Msr)
