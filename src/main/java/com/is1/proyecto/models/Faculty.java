package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("faculties") // Esta anotación asocia explícitamente el modelo 'Faculty' con la tabla
                    // 'faculties' en la DB.

public class Faculty extends Model {
    // No hace falta poner los atributos, ActiveJDBC los saca de la DB
}
