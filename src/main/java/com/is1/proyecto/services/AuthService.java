package com.is1.proyecto.services;
import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.models.User;


public class AuthService {

public User authenticate(String username, String plainTextPassword) {

            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                throw new IllegalArgumentException("Usuario o contraseña vacio");
            }

            User ac = User.findFirst("name = ?", username);
            if (ac == null) {
               throw new IllegalArgumentException("Usuario o contraseñas incorrectos");
            }

            // Obtiene la contraseña hasheada almacenada en la base de datos
            String storedHashedPassword = ac.getString("password");
            
            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                return ac;

            } else {
                 throw new IllegalArgumentException("Usuario o contraseñas incorrectos");
                
            }

    }
}
    

