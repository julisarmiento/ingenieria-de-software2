package com.is1.proyecto.services;
import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.models.Professor;
import com.is1.proyecto.models.User;
import com.is1.proyecto.services.EmailService;

import java.time.LocalDateTime;

public class ProfessorService {

    // Creamos un método que reciba los parámetros necesarios
    public void createProfessor(String nombre, String apellido, String correo, String dni, String token, LocalDateTime exp) {

        // Validaciones básicas
        if (nombre == null || nombre.isEmpty() ||
                apellido == null || apellido.isEmpty() ||
                correo == null || correo.isEmpty() ||
                dni == null || dni.isEmpty()) {

            throw new IllegalArgumentException("Faltan campos obligatorios");

        }

        if (!dni.matches("^[1-9]\\d{6,8}$")) {
            throw new IllegalArgumentException("El DNI debe ser un número positivo de entre 7 y 9 dígitos.");
        }

        if (!correo.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
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


        try{
            Base.openTransaction();
            User newUser = new User();
            newUser.set("name", username);
            newUser.set("password", hashedPassword);
            newUser.set("role", "profesor");
            newUser.saveIt();

            int userId = newUser.getInteger("id");
            
            Professor prof = new Professor();
            prof.set("id", userId);
            prof.set("name", nombre);
            prof.set("surname", apellido);
            prof.set("mail", correo);
            prof.set("dni", dni);
            prof.set("confirmUser", false);
            prof.set("token", token);
            prof.setExpireDateToken(exp);
            prof.insert();
            
            Base.commitTransaction();

            String userIdString = String.valueOf(userId);
            EmailService.enviarCorreoConfirmacion(userIdString, correo, token);

        }catch(Exception e){
            Base.rollbackTransaction();
            throw new RuntimeException("Error al crear profesor: " + e.getMessage(), e);
        }
    }
    
    public String deleteProfessor(String id) {
       
        Professor prof = Professor.findFirst("id = ?", id);
        if(prof == null){
            throw new IllegalArgumentException("El profesor no existe");
        }

        User user = User.findFirst("id = ?", id);
        if(user == null){
            throw new IllegalArgumentException("El usuario no existe");
        }

        String nameProfessor = prof.getString("name");

        try{
            Base.openTransaction();
            
            user.delete();
            prof.delete();
        
            Base.commitTransaction();
            return nameProfessor;

        }catch(Exception e){
            Base.rollbackTransaction();
            throw new RuntimeException("Error al eliminar al profesor", e);
        }

    }
}
