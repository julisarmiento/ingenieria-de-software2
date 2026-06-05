package com.is1.proyecto.models;

import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("students")
public class Student extends Model {

    public List<Enrollment> getEnrollments() {
        return Enrollment.where("student_id = ?", getId());
    }

    public StudentProgram getProgram() {
        return StudentProgram.findFirst("student_id = ?", getId());
    }

    public Person getPerson() {
        return Person.findById(this.getId());
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getSurname() {
        return getString("surname");
    }

    public void setSurname(String surname) {
        set("surname", surname);
    }

    public String getMail() {
        return getString("mail");
    }

    public void setMail(String mail) {
        set("mail", mail);
    }

    public String getDni() {
        return getString("dni");
    }

    public void setDni(String dni) {
        set("dni", dni);
    }
}
