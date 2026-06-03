package com.is1.proyecto.services;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.ProgramOfStudy;

public class ProgramOfStudyService {
    public static ProgramOfStudy createProgramOfStudyService(Integer career_id, Integer total_subjects,
            Integer mandatory_subjects, Integer elective_subjects, Integer year_version) {

        if (career_id == null || total_subjects == null || mandatory_subjects == null || elective_subjects == null) {
            throw new ValidationException("Faltan campos obligatorios");
        }

        try {
            Base.openTransaction();
            ProgramOfStudy.update("status = 'OBSOLETO'", "career_id = ? AND status = 'ACTIVO'", career_id);
            ProgramOfStudy pos = new ProgramOfStudy();
            pos.set("career_id", career_id);
            ProgramOfStudy.update("status = 'OBSOLETO'", "career_id = ? AND status = 'ACTIVO'", career_id);
            pos.set("total_subjects", total_subjects);
            pos.set("mandatory_subjects", mandatory_subjects);
            pos.set("elective_subjects", elective_subjects);
            pos.set("year_version", year_version);
            pos.saveIt();
            Base.commitTransaction();
            return pos;
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al crear el Plan de estudio: " + e.getMessage(), e);
        }
    }

    public static void deleteProgramOfStudyService(Integer id) {
        try {
            Base.openTransaction();
            ProgramOfStudy plan = ProgramOfStudy.findFirst("id = ?", id);
            if (plan == null) {
                throw new IllegalArgumentException("No se encuentra el plan");
            }
            plan.delete();
            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al borrar el Plan de estudio: " + e.getMessage(), e);
        }
    }
}
