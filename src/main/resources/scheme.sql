-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria autoincremental para SQLite
    name TEXT NOT NULL UNIQUE,          -- Nombre de usuario (TEXT es el tipo de cadena recomendado para SQLite), con restricción UNIQUE
    password TEXT NOT NULL,           -- Contraseña hasheada (TEXT es el tipo de cadena recomendado para SQLite)
    role TEXT NOT NULL DEFAULT       -- Rol que determina a un usuario o administrador
);


DROP TABLE IF EXISTS professors;

CREATE TABLE professors (
    id INTEGER PRIMARY KEY,     
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    dni INTEGER NOT NULL UNIQUE, 
    FOREIGN KEY (id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

HEAD

DROP TABLE IF EXISTS faculty;
altaTablaPrerequisiteCourse-Yaideem

CREATE TABLE faculty (
    id INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL
);

DROP TABLE IF EXISTS subject;

CREATE TABLE subject (
    id INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL
);

DROP TABLE IF EXISTS prerequisiteCourse;

CREATE TABLE prerequisiteCourse (
    id TEXT PRIMARY KEY,
    isPrerequisite BOOLEAN NOT NULL
);
DROP TABLE IF EXISTS finalNote;
CREATE TABLE finalNote (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT,
    student_dni INTEGER,
    subject_id INTEGER,
    FOREIGN KEY (student_dni) REFERENCES professors(id), 
    FOREIGN KEY (subject_id) REFERENCES subject(id)
);