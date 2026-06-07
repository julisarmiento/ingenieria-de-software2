package com.is1.proyecto.services;

import org.javalite.activejdbc.Base;

import com.is1.proyecto.models.Enrollment;
import com.is1.proyecto.models.ExamEnrollment;
import com.is1.proyecto.models.ExamTable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ExamTableService {

    public Map<String, Object> createExamTable(int professorId,
            int subjectId,
            int careerId,
            String examDate,
            String location) {
        Map<String, Object> result = new HashMap<>();

        try {
            LocalDate date = LocalDate.parse(examDate);
            if (date.isBefore(LocalDate.now())) {
                result.put("ok", false);
                result.put("error", "La fecha del examen no puede ser en el pasado.");
                return result;
            }
        } catch (Exception e) {
            result.put("ok", false);
            result.put("error", "Formato de fecha inválido. Usá YYYY-MM-DD.");
            return result;
        }

        ExamTable mesa = new ExamTable();
        mesa.set("professor_id", professorId)
                .set("subject_id", subjectId)
                .set("career_id", careerId)
                .set("exam_date", examDate)
                .set("status", "OPEN");

        if (location != null && !location.isBlank()) {
            mesa.set("location", location);
        }

        if (mesa.save()) {
            result.put("ok", true);
            result.put("examTable", mesa);
        } else {
            result.put("ok", false);
            result.put("error", mesa.errors().toString());
        }

        return result;
    }

    public Map<String, Object> gradeStudent(int enrollmentId,
            int professorId,
            int calification) {
        Map<String, Object> result = new HashMap<>();

        if (calification < 1 || calification > 10) {
            result.put("ok", false);
            result.put("error", "La calificación debe estar entre 1 y 10.");
            return result;
        }

        ExamEnrollment examEnrollment = ExamEnrollment.findById(enrollmentId);
        if (examEnrollment == null) {
            result.put("ok", false);
            result.put("error", "Inscripción no encontrada.");
            return result;
        }

        ExamTable mesa = ExamTable.findById(examEnrollment.getInteger("exam_table_id"));
        if (mesa == null || mesa.getInteger("professor_id") != professorId) {
            result.put("ok", false);
            result.put("error", "No tenés permiso para calificar esta mesa.");
            return result;
        }

        if (mesa.isCancelled()) {
            result.put("ok", false);
            result.put("error", "No se puede calificar una mesa cancelada.");
            return result;
        }

        if (examEnrollment.get("calification") != null) {
            result.put("ok", false);
            result.put("error", "Este alumno ya tiene una nota cargada.");
            return result;
        }

        boolean approved = calification >= 5;
        String newCondition;

        if (approved) {
            newCondition = "Approved";
        } else {
            newCondition = "Failed";
        }

        examEnrollment.set("calification", calification)
                .set("condition", newCondition)
                .set("graded_at", LocalDateTime.now().toString())
                .saveIt();

       
        if (approved) {
            approveEnrollment(
                    examEnrollment.getInteger("student_id"),
                    examEnrollment.getInteger("plan_subject_id"),
                    calification);
        }
        

        result.put("ok", true);
        result.put("approved", approved);
        result.put("examEnrollment", examEnrollment);
        return result;
    }

    private void approveEnrollment(int studentId, int planSubjectId, int calification) {
        Enrollment enrollment = Enrollment.findActiveForExam(studentId, planSubjectId);
        if (enrollment != null) {
            enrollment.approve(calification);
        }
    }


    public Map<String, Object> closeExamTable(int examTableId, int professorId) {
        return changeStatus(examTableId, professorId, "CLOSED");
    }

    

    public Map<String, Object> cancelExamTable(int examTableId, int professorId) {
        return changeStatus(examTableId, professorId, "CANCELLED");
    }

    private Map<String, Object> changeStatus(int examTableId, int professorId, String newStatus) {
        Map<String, Object> result = new HashMap<>();

        ExamTable mesa = ExamTable.findById(examTableId);

        if (mesa == null) {
            result.put("ok", false);
            result.put("error", "Mesa no encontrada.");
            return result;
        }

        if (mesa.getInteger("professor_id") != professorId) {
            result.put("ok", false);
            result.put("error", "No tenés permiso para modificar esta mesa.");
            return result;
        }

        if (mesa.isCancelled()) {
            result.put("ok", false);
            result.put("error", "La mesa ya está cancelada.");
            return result;
        }

        if ("CLOSED".equals(newStatus) && mesa.isClosed()) {
            result.put("ok", false);
            result.put("error", "La mesa ya está cerrada.");
            return result;
        }

        mesa.set("status", newStatus).saveIt();
        result.put("ok", true);
        result.put("examTable", mesa);
        return result;
    }
}