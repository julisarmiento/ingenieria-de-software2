-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS scheduleCareers;
DROP TABLE IF EXISTS scheduleProfessors;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS studentSubjectStatus;
DROP TABLE IF EXISTS finalNotes;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS periods;
DROP TABLE IF EXISTS student_programs;
DROP TABLE IF EXISTS prerequisites;
DROP TABLE IF EXISTS planSubjects;
DROP TABLE IF EXISTS student_programs;
DROP TABLE IF EXISTS programOfStudies;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS careers;
DROP TABLE IF EXISTS faculties;
DROP TABLE IF EXISTS professors;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS persons;
DROP TABLE IF EXISTS examEnrollments;
DROP TABLE IF EXISTS examTables;


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
    career_id INTEGER,
    mail TEXT NOT NULL UNIQUE,
    isFreshman BOOLEAN NOT NULL,
    FOREIGN KEY (career_id) REFERENCES careers(id),
    FOREIGN KEY (id) REFERENCES persons(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE professors (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    mail TEXT NOT NULL UNIQUE,
    dni TEXT NOT NULL UNIQUE,
    confirmUser BOOLEAN,
    token TEXT,
    expireDateToken DATETIME,
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
    name TEXT NOT NULL,
    career_id INTEGER NOT NULL,
    FOREIGN KEY (career_id) REFERENCES careers(id) ON DELETE CASCADE
);

CREATE TABLE programOfStudies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    career_id INTEGER NOT NULL,
    total_subjects INTEGER NOT NULL,
    mandatory_subjects INTEGER NOT NULL,
    elective_subjects INTEGER NOT NULL,
    year_version INTEGER NOT NULL,
    status TEXT DEFAULT 'ACTIVO' CHECK(status IN ('ACTIVO', 'OBSOLETO')),
    FOREIGN KEY (career_id) REFERENCES careers(id) ON DELETE CASCADE
);

CREATE TABLE planSubjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    programOfStudy_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    hours INTEGER NOT NULL,
    period TEXT DEFAULT 'CUATRIMESTRAL',
    is_elective BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (programOfStudy_id) REFERENCES programOfStudies(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE prerequisites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plan_subject_id INTEGER NOT NULL,
    required_subject_id INTEGER NOT NULL,
    req_type TEXT CHECK(req_type IN ('CURSAR_REGULAR', 'CURSAR_APROBADA', 'RENDIR_REGULAR', 'RENDIR_APROBADA')),
    FOREIGN KEY (plan_subject_id) REFERENCES planSubjects(id) ON DELETE CASCADE,
    FOREIGN KEY (required_subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE student_programs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL UNIQUE,
    program_of_study_id INTEGER NOT NULL,
    enrolled_at TEXT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (program_of_study_id) REFERENCES programOfStudies(id)
);

CREATE TABLE enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    plan_subject_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'CURSANDO' CHECK(status IN ('CURSANDO','APROBADA','DESAPROBADA','LIBRE')),
    note REAL,   
    period_id INTEGER, 
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_subject_id) REFERENCES planSubjects(id) ON DELETE RESTRICT,
    FOREIGN KEY (period_id) REFERENCES periods(id),
    UNIQUE(student_id, plan_subject_id)
);

CREATE TABLE finalNotes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dateTaken TEXT,
    calification INTEGER,
    condition TEXT CHECK(condition IN ('Non-enrolled', 'Enrolled', 'Promoted')),
    professor_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    FOREIGN KEY (professor_id) REFERENCES professors(id), 
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);


CREATE TABLE studentSubjectStatus (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    plan_subject_id INTEGER NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('INSCRIPTO', 'REGULAR', 'APROBADA', 'LIBRE')),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_subject_id) REFERENCES planSubjects(id) ON DELETE CASCADE,
    UNIQUE(student_id, plan_subject_id)
);

CREATE TABLE schedule (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    current_year INTEGER,
    subject_id TEXT NOT NULL,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE scheduleProfessors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_id INTEGER,
    professor_id INTEGER NOT NULL,
    role TEXT DEFAULT 'Docente Designado',
    FOREIGN KEY (schedule_id) REFERENCES schedule(id),
    FOREIGN KEY (professor_id) REFERENCES professors(id)
);

CREATE TABLE scheduleCareers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_id INTEGER,
    career_id INTEGER NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES schedule(id),
    FOREIGN KEY (career_id) REFERENCES careers(id)
);

CREATE TABLE examTables (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    career_id INTEGER NOT NULL,
    professor_id INTEGER NOT NULL,
    exam_date TEXT NOT NULL,          
    location TEXT,                    
    status TEXT NOT NULL DEFAULT 'OPEN'
        CHECK(status IN ('OPEN', 'CLOSED', 'CANCELLED')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (career_id) REFERENCES careers(id),
    FOREIGN KEY (professor_id) REFERENCES professors(id)
);

CREATE TABLE examEnrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    exam_table_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    condition TEXT NOT NULL DEFAULT 'Enrolled'
        CHECK(condition IN ('Enrolled', 'Absent', 'Approved', 'Failed')),
    calification INTEGER,             
    graded_at DATETIME,
    FOREIGN KEY (exam_table_id) REFERENCES examTables(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id),
    UNIQUE(exam_table_id, student_id) -- un alumno no se inscribe dos veces
);


