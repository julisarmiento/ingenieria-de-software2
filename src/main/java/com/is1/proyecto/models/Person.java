package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("persons") // Esta anotación asocia explícitamente el modelo 'Person' con la tabla
                  // 'persons' en la DB.
public class Person extends Model {
    // No hace falta poner los atributos, ActiveJDBC los saca de la DB
}
