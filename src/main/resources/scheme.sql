-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    name TEXT NOT NULL UNIQUE,          
    password TEXT NOT NULL,           
    role TEXT NOT NULL DEFAULT, 'user' -- Agregué el valor por defecto 'user'
    FOREIGN KEY (id) REFERENCES persons(id)
);

DROP TABLE IF EXISTS professors;
CREATE TABLE professors (
    id INTEGER PRIMARY KEY,
    name_and_surname TEXT NOT NULL,
    mail TEXT NOT NULL UNIQUE,
    dni TEXT NOT NULL UNIQUE,
    FOREIGN KEY (id) REFERENCES persons(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

DROP TABLE IF EXISTS careers;
CREATE TABLE careers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    faculty_id INTEGER NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

DROP TABLE IF EXISTS faculties;
CREATE TABLE faculties (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

DROP TABLE IF EXISTS subjects;
CREATE TABLE subjects (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

DROP TABLE IF EXISTS prerequisiteCourses;
CREATE TABLE prerequisiteCourses (
    id TEXT PRIMARY KEY,
    isPrerequisite BOOLEAN NOT NULL
);

DROP TABLE IF EXISTS periods;
CREATE TABLE periods (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    year INTEGER NOT NULL,
    term TTerm CHECK(term IN ('FIRST', 'SECOND'))
);

DROP TABLE IF EXISTS programOfStudy;
CREATE TABLE programOfStudy (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subjectName TEXT NOT NULL,
    subjectType TSubject CHECK(subjectType IN ('Required', 'Elective')),
    year INTEGER NOT NULL,
    hours INTEGER,
    curseReq TEXT,
    examReq TEXT,
    faculty_id INTEGER NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

DROP TABLE IF EXISTS finalNotes;
CREATE TABLE finalNotes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dateTaken TEXT,
    calification INTEGER,
    condition TCondition CHECK(condition IN ('Non-enrolled', 'Enrolled', 'Promoted')),
    professor_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    FOREIGN KEY (professor_id) REFERENCES professors(id), 
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

DROP TABLE IF EXISTS students;
CREATE TABLE students (
    id INTEGER PRIMARY KEY,
    dni TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    age INTEGER NOT NULL,
    phoneNum TEXT NOT NULL,
    mail TEXT NOT NULL UNIQUE,
    isFreshman BOOLEAN NOT NULL,
    FOREIGN KEY (id) REFERENCES persons(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

DROP TABLE IF EXISTS persons;
CREATE TABLE persons (
    id INTEGER PRIMARY KEY,
    dni TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    age INTEGER NOT NULL,
    phoneNum TEXT NOT NULL
);
