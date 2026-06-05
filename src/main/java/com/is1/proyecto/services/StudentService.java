package com.is1.proyecto.services;

import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.exceptions.AlreadyExistsException;
import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.Student;
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

        User existing = User.findFirst("name = ?", username);
        if (existing != null) {
            throw new AlreadyExistsException("El usuario no está disponible");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        try{
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
                s.set("age", Integer.parseInt(ageStr)); // Convertimos la edad a número entero
                s.set("phoneNum", phoneNum); 
                s.set("isFreshman", true);
                s.insert();
                Base.commitTransaction();
                return userId;

        }catch(Exception e){
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
    student.set("career_id", careerId);
    student.saveIt();
}
}
