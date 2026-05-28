-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS finalNotes;
DROP TABLE IF EXISTS periods;
DROP TABLE IF EXISTS prerequisites;
DROP TABLE IF EXISTS planSubjects;
DROP TABLE IF EXISTS programOfStudies;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS careers;
DROP TABLE IF EXISTS faculties;
DROP TABLE IF EXISTS professors;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS persons;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite

CREATE TABLE persons (
    id INTEGER PRIMARY KEY,
    dni TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    age INTEGER NOT NULL,
    phoneNum TEXT NOT NULL
);

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    name TEXT NOT NULL UNIQUE,          
    password TEXT NOT NULL,           
    role TEXT NOT NULL DEFAULT 'user',
    FOREIGN KEY (id) REFERENCES persons(id)
);

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

CREATE TABLE professors (
    id INTEGER PRIMARY KEY,
    name_and_surname TEXT NOT NULL,
    mail TEXT NOT NULL UNIQUE,
    dni TEXT NOT NULL UNIQUE,
    FOREIGN KEY (id) REFERENCES persons(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE faculties (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE careers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    faculty_id INTEGER NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE subjects (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE periods (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    year INTEGER NOT NULL,
    term TTerm CHECK(term IN ('FIRST', 'SECOND'))
);
    
CREATE TABLE programOfStudies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    career_id INTEGER NOT NULL,
    total_hours INTEGER NOT NULL,
    mandatory_hours INTEGER NOT NULL,
    elective_hours INTEGER NOT NULL,
    FOREIGN KEY (career_id) REFERENCES careers(id) ON DELETE CASCADE
);

CREATE TABLE planSubjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    programOfStudy_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    hours INTEGER NOT NULL,
    is_elective BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (programOfStudy_id) REFERENCES programOfStudies(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE prerequisites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plan_subject_id INTEGER NOT NULL,
    required_subject_id INTEGER NOT NULL,
    req_type TEXT CHECK(req_type IN ('COURSE', 'EXAM')),
    FOREIGN KEY (plan_subject_id) REFERENCES plan_subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (required_subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

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

