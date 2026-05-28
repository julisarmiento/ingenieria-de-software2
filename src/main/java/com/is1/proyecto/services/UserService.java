package com.is1.proyecto.services;

import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.models.User;
import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.exceptions.AlreadyExistsException;

public class UserService {
    public User registerUser(String name, String password){
         
            if (name == null || name.isEmpty()){
                throw new ValidationException("El nombre no puede estar vacio");
            }
                
            if (password == null || password.isEmpty()) {
                throw new ValidationException("La contraseña no puede estar vacia");
            }
   
        User existing = User.findFirst("name = ?", name);
            if (existing != null) {
                throw new AlreadyExistsException("El usuario no está disponible");
            }   

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
             User user = new User(); 

                user.set("name", name); 
                user.set("password", hashedPassword); 
                user.saveIt(); 

        return user;
    }

}   
