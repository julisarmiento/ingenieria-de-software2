package com.is1.proyecto.services;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.ProgramOfStudy;

public class ProgramOfStudyService {
    public static ProgramOfStudy createProgramOfStudyService(Integer career_id, Integer total_hours,
            Integer mandatory_hours, Integer elective_hours) {

        if (career_id == null || total_hours == null || mandatory_hours == null || elective_hours == null) {
            throw new ValidationException("Faltan campos obligatorios");
        }

        try {
            Base.openTransaction();
            ProgramOfStudy pos = new ProgramOfStudy();
            pos.set("career_id", career_id);
            pos.set("total_hours", total_hours);
            pos.set("mandatory_hours", mandatory_hours);
            pos.set("elective_hours", elective_hours);
            pos.saveIt();
            Base.commitTransaction();
            return pos;
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al crear el Plan de estudio: " + e.getMessage(), e);
        }
    }

    public static void deleteProgramOfStudyService(Integer id) {
        ProgramOfStudy plan = ProgramOfStudy.findFirst("id = ?", id);
        if (plan == null) {
            throw new IllegalArgumentException("No se encuentra el plan seleccionado");
        }
        plan.delete();
    }
}
