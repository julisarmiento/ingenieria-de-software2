package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("enrollments")
public class Enrollment extends Model {
    public static final String CURSANDO = "CURSANDO";
    public static final String APROBADA = "APROBADA";
    public static final String DESAPROBADA = "DESAPROBADA";
    public static final String LIBRE = "LIBRE";
}
