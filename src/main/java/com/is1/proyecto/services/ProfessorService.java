package com.is1.proyecto.services;

import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.models.Professor;
import com.is1.proyecto.models.User;

public class ProfessorService {

    // Creamos un método que reciba los parámetros necesarios
    public void createProfessor(String nombre, String apellido, String correo, String dni) {
        // Validaciones básicas
        if (nombre == null || nombre.isEmpty() ||
                apellido == null || apellido.isEmpty() ||
                correo == null || correo.isEmpty() ||
                dni == null || dni.isEmpty()) {

            throw new IllegalArgumentException("Faltan campos obligatorios");

        }

        if (dni.length() < 4) {
            throw new IllegalArgumentException("El DNI debe tener al menos 4 caracteres");
        }

        if (!correo.contains("@") || !correo.contains(".")) {
            throw new IllegalArgumentException("Correo no valido");

        }
        // Verificar DNI único
        if (Professor.findFirst("dni = ?", dni) != null) {
            throw new IllegalArgumentException("El DNI ya está registrado");
        }

        // Verificar correo único
        if (Professor.findFirst("mail = ?", correo) != null) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        // Creacion = primera letra del nombre + apellido
        String username = nombre.substring(0, 1).toUpperCase() +
            apellido;

        // Contraseña = últimos 4 dígitos del DNI
        String last4 = dni.substring(dni.length() - 4);
        String hashedPassword = BCrypt.hashpw(last4, BCrypt.gensalt());

        // insercion para user
        User newUser = new User();
        newUser.set("name", username);
        newUser.set("password", hashedPassword);
        newUser.set("role", "professor");
        newUser.saveIt();

        int userId = newUser.getInteger("id");

        // insercion para profesor
        Professor prof = new Professor();
        prof.set("id", userId);
        prof.set("name", nombre);
        prof.set("surname", apellido);
        prof.set("mail", correo);
        prof.set("dni", dni);
        prof.insert();
    }
}
