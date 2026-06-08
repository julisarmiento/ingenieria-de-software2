package com.is1.proyecto.services;

import java.util.List;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.Enrollment;
import com.is1.proyecto.models.PlanSubject;
import com.is1.proyecto.models.Prerequisite;
import com.is1.proyecto.models.Subject;

public class EnrollmentService {

    public Enrollment inscribir(Integer studentId, Integer planSubjectId) {
        PlanSubject ps = PlanSubject.findById(planSubjectId);
        if (ps == null) {
            throw new ValidationException("Materia no encontrada");
        }

        if (Enrollment.findFirst("student_id = ? AND plan_subject_id = ?", studentId, planSubjectId) != null) {
            throw new ValidationException("Ya estás inscripto en esta materia.");
        }

        validarCorrelativas(studentId, planSubjectId);

        try {
            Base.openTransaction();
            Enrollment enroll = new Enrollment();
            enroll.set("student_id", studentId);
            enroll.set("plan_subject_id", planSubjectId);
            enroll.set("status", Enrollment.CURSANDO);
            enroll.saveIt();
            Base.commitTransaction();
            return enroll;
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al inscribir: " + e.getMessage(), e);
        }
    }

    private void validarCorrelativas(Integer studentId, Integer planSubjectId) {
        List<Prerequisite> prereqs = Prerequisite.where("plan_subject_id = ?", planSubjectId);

        for (Prerequisite prereq : prereqs) {
            Integer requiredSubjectId = prereq.getInteger("required_subject_id");
            String reqType = prereq.getString("req_type");

            Enrollment cumple = findEnrollmentBySubject(studentId, requiredSubjectId);

            boolean ok = false;

            if (cumple != null) {
                String estado = cumple.getString("status");

                if (reqType.equals("CURSAR_REGULAR")) {
                    ok = estado.equals(Enrollment.CURSANDO) || estado.equals(Enrollment.APROBADA);
                } else if (reqType.equals("CURSAR_APROBADA")) {
                    ok = estado.equals(Enrollment.APROBADA);
                } else if (reqType.equals("RENDIR_REGULAR")) {
                    ok = !estado.equals(Enrollment.LIBRE);
                } else if (reqType.equals("RENDIR_APROBADA")) {
                    ok = estado.equals(Enrollment.APROBADA);
                }
            }

            if (!ok) {
                Subject required = Subject.findById(requiredSubjectId);
                String nombre = required != null ? required.getString("name") : "#" + requiredSubjectId;
                String descripcion;
                if (reqType.equals("CURSAR_REGULAR")) {
                    descripcion = "Para cursar esta materia necesitás tener regular: ";
                } else if (reqType.equals("CURSAR_APROBADA")) {
                    descripcion = "Para cursar esta materia necesitás tener aprobada: ";
                } else if (reqType.equals("RENDIR_REGULAR")) {
                    descripcion = "Para rendir esta materia necesitás tener regular: ";
                } else if (reqType.equals("RENDIR_APROBADA")) {
                    descripcion = "Para rendir esta materia necesitás tener aprobada: ";
                } else {
                    descripcion = "Correlativa no cumplida: ";
                }
                throw new ValidationException(descripcion + nombre);
            }
        }

    }

    private Enrollment findEnrollmentBySubject(Integer studentId, Integer subjectId) {
        // JOIN implícito: enrollments -> planSubjects -> subjects
        return Enrollment.findFirst(
                "student_id = ? AND plan_subject_id IN " + "(SELECT id FROM planSubjects WHERE subject_id = ?)",
                studentId, subjectId);
    }
}
