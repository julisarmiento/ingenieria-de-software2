package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("studentSubjectStatus")
public class EnrollmentModel extends Model {

    public int getStudentId()     { return getInteger("student_id"); }
    public int getSubjectId()     { return getInteger("subject_id"); }
    public int getPlanSubjectId() { return getInteger("plan_subject_id"); }
    public String getStatus()     { return getString("status"); }
}