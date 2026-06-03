package com.is1.proyecto.services;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.PlanSubject;
import com.is1.proyecto.models.Prerequisite;
import com.is1.proyecto.models.ProgramOfStudy;

public class PlanSubjectService {

    public void createPlanSubject(Integer programId, Integer subjectId, Integer year, Integer hours, boolean isElective,
            String[] curseReqs, String[] examReqs) {

        if (programId == null || subjectId == null || year == null || hours == null) {
            throw new ValidationException("Faltan campos obligatorios");
        }

        PlanSubject repetida = PlanSubject.findFirst("programOfStudy_id = ? AND subject_id = ?", programId, subjectId);
        if (repetida != null) {
            throw new ValidationException("Esta materia ya se encuentra cargada en este plan de estudios.");
        }
        if (curseReqs != null) {
            for (String subID : curseReqs) {
                if (subID.equals(subjectId.toString())) {
                    throw new ValidationException("Una materia no puede ser requisito de cursada de sí misma.");
                }
            }
        }
        if (examReqs != null) {
            for (String subID : examReqs) {
                if (subID.equals(subjectId.toString())) {
                    throw new ValidationException("Una materia no puede ser requisito de examen de sí misma.");
                }
            }
        }
        ProgramOfStudy program = ProgramOfStudy.findById(programId);

        int limiteTotal = program.getInteger("mandatory_subjects") + program.getInteger("elective_subjects");
        long cantidadActual = PlanSubject.count("programOfStudy_id = ?", programId);

        if (cantidadActual >= limiteTotal) {
            throw new ValidationException("¡El plan ya alcanzó su límite de materias!");
        }

        try {
            Base.openTransaction();
            PlanSubject ps = new PlanSubject();
            ps.set("programOfStudy_id", programId);
            ps.set("subject_id", subjectId);
            ps.set("year", year);
            ps.set("hours", hours);
            ps.set("is_elective", isElective ? 1 : 0);
            ps.saveIt();
            if (curseReqs != null) {
                for (String subID : curseReqs) {
                    guardarPrerrequisitos(ps.getId(), Integer.parseInt(subID), "COURSE");
                }
            }
            if (examReqs != null) {
                for (String subID : examReqs) {
                    guardarPrerrequisitos(ps.getId(), Integer.parseInt(subID), "EXAM");
                }
            }
            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al crear el Cronograma de la materia: " + e.getMessage(), e);
        }
    }

    private void guardarPrerrequisitos(Object psID, int requiredSubjectID, String tipo) {
        Prerequisite p = new Prerequisite();
        p.set("plan_subject_id", psID);
        p.set("required_subject_id", requiredSubjectID);
        p.set("req_type", tipo);
        p.saveIt();
    }

}
