package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("students")
public class Student extends Model {
    public String getName() { return getString("name"); }
    public void setName(String name) { set("name", name); }

    public String getSurname() { return getString("surname"); }
    public void setSurname(String surname) { set("surname", surname); }

    public String getMail() { return getString("mail"); }
    public void setMail(String mail) { set("mail", mail); }

    public String getDni() { return getString("dni"); }
    public void setDni(String dni) { set("dni", dni); }
}
