package com.is1.proyecto.services;

import org.mindrot.jbcrypt.BCrypt;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.User;

public class StudentService {

    public void createStudent(String username, String password, String name, String surname, 
                              String dni, String mail, int age, String phoneNum, boolean isFreshman) {
        
        // Validaciones básicas
        if (username == null || username.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            surname == null || surname.trim().isEmpty() ||
            dni == null || dni.trim().isEmpty() ||
            mail == null || mail.trim().isEmpty() ||
            phoneNum == null || phoneNum.trim().isEmpty()) {
            throw new IllegalArgumentException("Faltan campos obligatorios.");
        }

        if (dni.length() < 4) {
            throw new IllegalArgumentException("El DNI debe tener al menos 4 caracteres");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío o contener solo espacios");
        }
        if (!mail.contains("@") || !mail.contains(".")) {
            throw new IllegalArgumentException("Correo no valido");
        }

        // Verificar datos únicos 
        if (User.findFirst("name = ?", username) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        if (Student.findFirst("dni = ?", dni) != null) {
            throw new IllegalArgumentException("El DNI ya está registrado.");
        }
        if (Student.findFirst("mail = ?", mail) != null) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }

        // Contraseña = últimos 4 dígitos del DNI
        String last4 = dni.substring(dni.length() - 4);
        String hashedPassword = BCrypt.hashpw(last4, BCrypt.gensalt());

        // 4. Inserción para user
        User u = new User();
        u.set("name", username);
        u.set("password", hashedPassword);
        u.set("role", "user");
        u.saveIt();

        int userId = u.getInteger("id");

        // Inserción para estudiante
        Student s = new Student();
        s.set("id", u.getId());
        s.set("name", name);
        s.set("surname", surname);
        s.set("dni", dni);
        s.set("mail", mail);
        s.set("age", age);
        s.set("phoneNum", phoneNum);
        s.set("isFreshman", isFreshman);
        s.insert();
    }
}