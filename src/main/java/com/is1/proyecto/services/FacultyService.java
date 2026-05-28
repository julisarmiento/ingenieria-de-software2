package com.is1.proyecto.services;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.exceptions.AlreadyExistsException;
import com.is1.proyecto.models.Faculty;

public class FacultyService {
    
    public void createFaculty(String name){
         if (name == null || name.isEmpty()) {
                throw new ValidationException("El nombre de la facultad no puede ser vacio");
            }

        if (Faculty.findFirst("name = ?", name) != null) {
                   throw new AlreadyExistsException("La facultad ya esta registrada");
        }

        Faculty newFaculty = new Faculty();
                newFaculty.set("name", name);
                newFaculty.saveIt();  

    }

public void deleteFaculty(String id){
     Faculty f = Faculty.findFirst("id = ?", id);

     if (f == null) {
         throw new ValidationException("La facultad no existe");
     }

     f.delete();
}
}
