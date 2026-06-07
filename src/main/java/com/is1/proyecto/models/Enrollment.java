package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("enrollments")
public class Enrollment extends Model {
    public static final String CURSANDO = "CURSANDO";
    public static final String APROBADA = "APROBADA";
    public static final String DESAPROBADA = "DESAPROBADA";
    public static final String LIBRE = "LIBRE";

    public boolean isCursando() {
        return "CURSANDO".equals(getString("status"));
    }

    public boolean isRegular() {
        return "REGULAR".equals(getString("status"));
    }

    public boolean isLibre() {
        return "LIBRE".equals(getString("status"));
    }

    public boolean isAprobada() {
        return "APROBADA".equals(getString("status"));
    }

    public boolean isDesaprobada() {
        return "DESAPROBADA".equals(getString("status"));
    }

    public boolean canTakeFinalExam() {
        return isRegular() || isLibre();
    }

    public int getStudentId() {
        return getInteger("student_id");
    }

    public int getPlanSubjectId() {
        return getInteger("plan_subject_id");
    }

    public String getStatus() {
        return getString("status");
    }

    public Double getNote() {
        return getDouble("note");
    }

    public Integer getPeriodId() {
        return getInteger("period_id");
    }

    public void approve(int note) {
        set("status", "APROBADA")
                .set("note", note)
                .saveIt();
    }

    public void markAsRegular() {
        set("status", "REGULAR").saveIt();
    }

    public void markAsLibre() {
        set("status", "LIBRE").saveIt();
    }

    public void markAsDesaprobada() {
        set("status", "DESAPROBADA").saveIt();
    }

    public static Enrollment findByStudentAndPlanSubject(int studentId, int planSubjectId) {
        return (Enrollment) findFirst(
                "student_id = ? AND plan_subject_id = ?",
                studentId, planSubjectId);
    }

    public static Enrollment findActiveForExam(int studentId, int planSubjectId) {
        return (Enrollment) findFirst(
                "student_id = ? AND plan_subject_id = ? AND status IN ('REGULAR', 'LIBRE')",
                studentId, planSubjectId);
    }

}
