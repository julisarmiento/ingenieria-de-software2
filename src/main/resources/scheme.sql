-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    name TEXT NOT NULL UNIQUE,          
    password TEXT NOT NULL,           
    role TEXT NOT NULL DEFAULT 'user' -- Agregué el valor por defecto 'user'
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


DROP TABLE IF EXISTS career;

CREATE TABLE career (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    faculty_id INTEGER NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
DROP TABLE IF EXISTS faculty;

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
); -- Se agregó el cierre de la tabla que faltaba

DROP TABLE IF EXISTS student;

CREATE TABLE student (
    dni TEXT PRIMARY KEY, 
    nYApellido TEXT NOT NULL, 
    edad INTEGER NOT NULL,
    numTel TEXT NOT NULL, 
    contact TEXT NOT NULL,
    ingresante BOOLEAN NOT NULL
);

DROP TABLE IF EXISTS persons;

CREATE TABLE persons (
    dni INTEGER PRIMARY KEY,
    nYApellido TEXT NOT NULL,
    edad INTEGER NOT NULL,
    numTel TEXT NOT NULL
);
