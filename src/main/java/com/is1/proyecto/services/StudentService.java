package com.is1.proyecto.services;

import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.exceptions.AlreadyExistsException;
import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.Enrollment;
import com.is1.proyecto.models.ProgramOfStudy;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.StudentCareers;
import com.is1.proyecto.models.StudentProgram;
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
            careerStudent.set("student_id", careerId);
            careerStudent.set("career_id", studentId);
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
}
