package com.is1.proyecto.services;

import org.javalite.activejdbc.Base;

import java.util.List;
import com.is1.proyecto.models.Prerequisite;
import com.is1.proyecto.models.PlanSubject;
import com.is1.proyecto.exceptions.ValidationException;

import com.is1.proyecto.models.ProgramOfStudy;

public class ProgramOfStudyService {
    public static ProgramOfStudy createProgramOfStudyService(Integer career_id, Integer total_subjects,
            Integer mandatory_subjects, Integer elective_subjects, Integer year_version) {

        if (career_id == null || total_subjects == null || mandatory_subjects == null || elective_subjects == null
                || year_version == null) {
            throw new ValidationException("Faltan campos obligatorios");
        }

        if (year_version < 0) {
            throw new ValidationException("El año de versión no puede ser negativo");
        }

        if (mandatory_subjects < 0) {
            throw new ValidationException("La cantidad de materias obligatorias no puede ser negativa");
        }

        if (elective_subjects < 0) {
            throw new ValidationException("La cantidad de materias optativas no puede ser negativa");
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

            if (plan != null) {
                List<PlanSubject> materiasDelPlan = PlanSubject
                        .where("programOfStudy_id = ?", id);

                for (PlanSubject ps : materiasDelPlan) {
                    Prerequisite.delete("plan_subject_id = ?", ps.getId());
                }

                PlanSubject.delete("programOfStudy_id = ?", id);

                plan.delete();
            }
            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al borrar el Plan de estudio: " + e.getMessage(), e);
        }
    }
}
