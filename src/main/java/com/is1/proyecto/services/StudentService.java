package com.is1.proyecto.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.exceptions.AlreadyExistsException;
import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.Enrollment;
import com.is1.proyecto.models.ExamEnrollment;
import com.is1.proyecto.models.ExamTable;
import com.is1.proyecto.models.PlanSubject;
import com.is1.proyecto.models.ProgramOfStudy;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.StudentCareers;
import com.is1.proyecto.models.StudentProgram;
import com.is1.proyecto.models.Subject;
import com.is1.proyecto.models.User;

public class StudentService {
    public int registerStudent(String username, String password, String name, String surname, String dni, String mail,
            String ageStr, String phoneNum) {

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                name == null || name.trim().isEmpty() ||
                surname == null || surname.trim().isEmpty() ||
                dni == null || dni.trim().isEmpty() ||
                mail == null || mail.trim().isEmpty() ||
                ageStr == null || ageStr.isEmpty() ||
                phoneNum == null || phoneNum.isEmpty()) {

            throw new ValidationException("Los campos no pueden estar vacios");

        }

        int edad = Integer.parseInt(ageStr);

        if (edad < 17) {

            throw new ValidationException("El estudiante debe tener al menos 17 años.");
        }

        // Verificamos si el nombre ingresado no es null y si solo contiene letras
        if (!name.matches("^[\\p{L} ]+$")) {
            throw new ValidationException("El nombre ingresado es invalido");
        }

        if (!dni.matches("^[1-9]\\d{6,8}$")) {
            throw new ValidationException("El DNI debe ser un número positivo de entre 7 y 9 dígitos.");
        }

        if (!password.matches("^[a-zA-Z0-9]+$")) {
            throw new ValidationException(
                    "La contraseña solo puede contener letras y números (sin espacios ni símbolos).");
        }

        if (!mail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$")) {
            throw new ValidationException("El formato del correo electrónico no es válido.");
        }

        User existing = User.findFirst("name = ?", username);
        if (existing != null) {
            throw new AlreadyExistsException("El usuario no está disponible");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try {
            Base.openTransaction();
            User user = new User();
            user.set("name", username);
            user.set("password", hashedPassword);
            user.set("role", "estudiante");
            user.saveIt();

            int userId = user.getInteger("id");

            Student s = new Student();
            s.set("id", userId); // Lo vinculamos usando el mismo ID
            s.set("name", name);
            s.set("surname", surname);
            s.set("dni", dni);
            s.set("mail", mail);
            s.set("age", edad);
            s.set("phoneNum", phoneNum);
            s.set("isFreshman", true);
            s.insert();
            Base.commitTransaction();
            return userId;

        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al registrar estudiante: " + e.getMessage(), e);
        }
    }

    public String deleteStudent(String id) {

        Student s = Student.findFirst("id = ?", id);
        if (s == null) {
            throw new IllegalArgumentException("El estudiante no existe");
        }

        User u = User.findFirst("id = ?", id);
        String name = s.getString("name") + " " + s.getString("surname");

        try {
            Base.openTransaction();

            if (u != null) {
                u.delete();
            }

            s.delete();

            Base.commitTransaction();

            return name;

        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al eliminar estudiante", e);
        }
    }

    public void assignCareer(int studentId, int careerId) {
        Student student = Student.findFirst("id = ?", studentId);
        if (student == null) {
            throw new IllegalArgumentException("Estudiante no encontrado.");
        }
        Career career = Career.findFirst("id = ?", careerId);
        if (career == null) {
            throw new IllegalArgumentException("Carrera no encontrada.");
        }
        ProgramOfStudy planActivo = ProgramOfStudy.findFirst("career_id = ? AND status = 'ACTIVO'", careerId);
        if (planActivo == null) {
            throw new IllegalArgumentException("No hay un plan de estudio activo para esta");
        }
        try {
            Base.openTransaction();

            student.set("career_id", careerId);
            student.saveIt();

            StudentCareers careerStudent = new StudentCareers();
            careerStudent.set("student_id", studentId);
            careerStudent.set("career_id", careerId);
            careerStudent.saveIt();

            StudentProgram sp = new StudentProgram();
            sp.set("student_id", studentId);
            sp.set("program_of_study_id", planActivo.getId());
            sp.set("enrolled_at", java.time.LocalDate.now().toString());
            sp.saveIt();

            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al asignar carrera: " + e.getMessage(), e);
        }
    }

    public void unenrollCareer(int studentId, int careerId) {
        Student student = Student.findFirst("id = ?", studentId);
        if (student == null) {
            throw new IllegalArgumentException("Estudiante no encontrado.");
        }
        Career career = Career.findFirst("id = ?", careerId);
        if (career == null) {
            throw new IllegalArgumentException("Carrera no encontrada.");
        }
        try {
            Base.openTransaction();

            StudentProgram sp = StudentProgram.findFirst("student_id = ?", studentId);
            if (sp != null)
                sp.delete();

            Enrollment.delete("student_id = ?", studentId);

            student.set("career_id", null).saveIt();

            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al desasignar carrera: " + e.getMessage(), e);
        }

    }

    public List<Map<String, Object>> getAvailableExamTables(int studentId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Una sola carrera guardada en students
        Student student = Student.findById(studentId);
        Integer careerId = student.getInteger("career_id");

        System.out.println("DEBUG career_id del alumno: " + careerId);

        if (careerId == null)
            return result;

        List<ExamTable> allTables = ExamTable.where(
                "status = ? AND career_id = ?", "OPEN", careerId);

        System.out.println("DEBUG total mesas abiertas: " + allTables.size());

        for (ExamTable examTable : allTables) {
            PlanSubject planSubject = (PlanSubject) PlanSubject.findFirst(
                    "subject_id = ?", examTable.getInteger("subject_id"));
            System.out.println("DEBUG planSubject para mesa " + examTable.getId() + ": " + planSubject);

            if (planSubject == null)
                continue;

            Enrollment enrollment = Enrollment.findActiveForExam(
                    studentId, planSubject.getInteger("id"));
            System.out.println("DEBUG enrollment del alumno: " + enrollment);

            if (enrollment == null)
                continue;

            ExamEnrollment alReadyEnrolled = ExamEnrollment.findByStudentAndExamTable(
                    studentId, examTable.getInteger("id"));
            System.out.println("DEBUG ya inscripto: " + alReadyEnrolled);

            if (alReadyEnrolled != null)
                continue;

            Subject subject = Subject.findById(examTable.getInteger("subject_id"));
            Career career = Career.findById(examTable.getInteger("career_id"));

            Map<String, Object> map = new HashMap<>();
            map.put("id", examTable.getId());
            map.put("subjectName", subject != null ? subject.getString("name") : "");
            map.put("careerName", career != null ? career.getString("name") : "");
            map.put("examDate", examTable.getString("exam_date"));
            map.put("location", examTable.getString("location"));
            result.add(map);
        }

        return result;
    }

    public Map<String, Object> enrollToExamTable(int studentId, int examTableId) {
        Map<String, Object> result = new HashMap<>();

        ExamTable mesa = ExamTable.findById(examTableId);
        if (mesa == null || !mesa.isOpen()) {
            result.put("ok", false);
            result.put("error", "La mesa no está disponible.");
            return result;
        }

        ExamEnrollment alReadyEnrolled = ExamEnrollment.findByStudentAndExamTable(studentId, examTableId);
        if (alReadyEnrolled != null) {
            result.put("ok", false);
            result.put("error", "Ya estás inscripto en esta mesa.");
            return result;
        }

        PlanSubject planSubject = (PlanSubject) PlanSubject.findFirst(
                "subject_id = ?", mesa.getInteger("subject_id"));

        if (planSubject == null) {
            result.put("ok", false);
            result.put("error", "No se encontró el plan de estudios para esta materia.");
            return result;
        }

        Enrollment enrollment = Enrollment.findActiveForExam(studentId, planSubject.getInteger("id"));
        if (enrollment == null) {
            result.put("ok", false);
            result.put("error", "No tenés condición para rendir esta materia.");
            return result;
        }

        ExamEnrollment nuevaInscripcion = new ExamEnrollment();
        nuevaInscripcion.set("exam_table_id", examTableId)
                .set("student_id", studentId)
                .set("condition", "Enrolled")
                .saveIt();

        result.put("ok", true);
        return result;
    }

    public List<Map<String, Object>> getMateriasAprobadas(int studentId) {
        List<Enrollment> aprobadas = Enrollment.where("student_id = ? AND status = ?", studentId, "APROBADA");
        List<Map<String, Object>> lista = new ArrayList<>();

        for (Enrollment enr : aprobadas) {
            PlanSubject ps = PlanSubject.findById(enr.getPlanSubjectId());
            Map<String, Object> map = new HashMap<>();
            if (ps != null) {
                Subject subject = Subject.findById(ps.getInteger("subject_id"));
                map.put("nombre_materia", subject != null ? subject.getString("name") : "Materia desconocida");
            } else {
                map.put("nombre_materia", "Materia desconocida");
            }
            map.put("nota", enr.getNote());
            lista.add(map);
        }
        return lista;

    }

    public List<Map<String, Object>> getMateriasCursando(int studentId) {
        List<Enrollment> cursando = Enrollment.where("student_id = ? AND status = ?", studentId, "CURSANDO");
        List<Map<String, Object>> lista = new ArrayList<>();

        for (Enrollment enr : cursando) {
            PlanSubject ps = PlanSubject.findById(enr.getPlanSubjectId());
            if (ps != null) {
                com.is1.proyecto.models.Subject subj = com.is1.proyecto.models.Subject.findById(ps.get("subject_id"));
                Map<String, Object> map = new HashMap<>();
                map.put("nombre_materia", subj != null ? subj.getString("name") : "Materia");
                // USAMOS EL ID COMO CÓDIGO YA QUE NO TENEMOS CAMPO 'CODE'
                map.put("codigo_materia", subj != null ? subj.getInteger("id") : "---");
                lista.add(map);
            }
        }
        return lista;
    }
}
