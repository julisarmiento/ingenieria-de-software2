package com.is1.proyecto.services;

import org.mindrot.jbcrypt.BCrypt;

import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.User;
import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.exceptions.UserAlreadyExistsException;

public class StudentService {
    public User registerStudent(String username, String password, String name, String surname, String dni, String mail, String ageStr, String phoneNum){
         
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty() || 
                name == null || name.trim().isEmpty() ||
                surname == null || surname.trim().isEmpty()||
                dni == null || dni.trim().isEmpty() || 
                mail == null || mail.trim().isEmpty() ||
                ageStr == null || ageStr.isEmpty() ||
                phoneNum == null || phoneNum.isEmpty()){

                throw new ValidationException("Los campos no pueden estar vacios");
            
            }

        /*
            AGREGAR VALIDACIONES - ANA
        */
   
        User existing = User.findFirst("name = ?", username);
            if (existing != null) {
                throw new UserAlreadyExistsException("El usuario no está disponible");
            }   

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
             User user = new User(); 

                user.set("name", username); // Asigna el nombre de usuario.
                user.set("password", hashedPassword); // Asigna la contraseña hasheada.
                user.set("role", "estudiante");
                user.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                int userId = user.getInteger("id");

                //Insercion en estudiante
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

        return user;
    }

}  
