package com.is1.proyecto.services;

import com.is1.proyecto.models.Career;
import com.is1.proyecto.models.Faculty;

public class CareerService {

    // Creamos un método que reciba los parámetros necesarios
    public void createCareer(String name, String faculty_id) {
        // Validaciones básicas
        if (name.isEmpty() || faculty_id.isEmpty()) {

            throw new IllegalArgumentException("Faltan campos obligatorios");

        }

        //Verificamos si el nombre ingresado no es null y si solo contiene letras
        if(name == null || !name.matches("^[\\p{L} ]+$")){
            throw new IllegalArgumentException("El nombre ingresado es invalido");
        }

        //Verificamos si el id ingresado no es null y si solo contiene numero enteros
        if(faculty_id == null || !faculty_id.matches("^[1-9]\\d*$")){
            throw new IllegalArgumentException("El id de la facultad es invalido");
        }

        //Convertimos el id de String a int
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
