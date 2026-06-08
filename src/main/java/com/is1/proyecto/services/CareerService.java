package com.is1.proyecto.services;

import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.Faculty;

public class CareerService {

    public void createCareer(String name, String faculty_id) {
        if (name.isEmpty() || faculty_id.isEmpty()) {

            throw new IllegalArgumentException("Faltan campos obligatorios");

        }

        if(name == null || !name.matches("^[\\p{L} ]+$")){
            throw new IllegalArgumentException("El nombre ingresado es invalido");
        }

        if(faculty_id == null || !faculty_id.matches("^[1-9]\\d*$")){
            throw new IllegalArgumentException("El id de la facultad es invalido");
        }

        int faculty_id_int = Integer.parseInt(faculty_id); 
        
        if(faculty_id_int <= 0){
            throw new IllegalArgumentException("El id de la facultad debe ser positivo");
        }

        if (Career.findFirst("name = ?", name) != null) {
            throw new IllegalArgumentException("La carrera ya está registrada");
        }

        if(Faculty.findFirst("id = ?", faculty_id) == null) {
            throw new IllegalArgumentException("No existe una facultad con el id: " + faculty_id);
        }

        Career newCareer = new Career();
        newCareer.set("name", name);
        newCareer.set("faculty_id", faculty_id_int);
        newCareer.saveIt();
    }

      
    public void deleteCareer(String id) {

        Career career = Career.findFirst("id = ?", id);

        if(career == null) {
            throw new IllegalArgumentException("No existe una Carrera con el id: " + id);
        } else {
                career.delete();
        }
    }

    
}
