package com.is1.proyecto.services;

import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.models.Professor;
import com.is1.proyecto.models.User;

public class ResetPasswordService {

    // Creamos un método que reciba los parámetros necesarios
    public void createPassword(String id, String password) {
        
        // Validaciones básicas
        if (id.isEmpty() || id == null || password.isEmpty() || password == null) {

            throw new IllegalArgumentException("Faltan campos obligatorios");

        }

        int idInt = Integer.parseInt(id);
        
        User userProfessor = User.findFirst("id = ?", idInt);
        Professor prof = Professor.findFirst("id = ?", idInt);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        userProfessor.set("password", hashedPassword);
        prof.setExpireDateToken(null);
        prof.set("token", null);
        prof.set("confirmUser", true);
        
        userProfessor.saveIt();
        prof.saveIt();
    }
}