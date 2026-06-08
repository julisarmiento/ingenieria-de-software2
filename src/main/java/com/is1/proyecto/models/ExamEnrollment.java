package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("examEnrollments")
public class ExamEnrollment extends Model {
    public boolean isEnrolled() {
        return "Enrolled".equals(getString("condition"));
    }

    public boolean isApproved() {
        return "Approved".equals(getString("condition"));
    }

    public boolean isFailed() {
        return "Failed".equals(getString("condition"));
    }

    public boolean isAbsent() {
        return "Absent".equals(getString("condition"));
    }

    public void approve(int calification) {
        set("condition", "Approved")
                .set("calification", calification)
                .saveIt();
    }

    public void fail() {
        set("condition", "Failed").saveIt();

    }

    public int getStudentId() {
        return getInteger("student_id");

    }

    public int getExamTableId() {
        return getInteger("exam_table_id");
    }

    public static ExamEnrollment findByStudentAndExamTable(int studentId, int examTableId) {
        return (ExamEnrollment) findFirst(
                "student_id = ? AND exam_table_id = ?",
                studentId, examTableId);
    }

    public String getCondition() {
        return getString("condition");
    }
}