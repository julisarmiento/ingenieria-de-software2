package com.is1.proyecto.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("professors")
public class Professor extends Model {

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

    public String getToken() {
        return getString("token");
    }

    public void setToken(String token) {
        set("token", token);
    }

    public LocalDateTime getExpireDateToken() {
        Timestamp exp = getTimestamp("expireDateToken");
        if(exp == null){
            return null;
        }
        return exp.toLocalDateTime();
    }

    public void setExpireDateToken(LocalDateTime exp) {
        if (exp == null) {
            set("expireDateToken", null);
        } else {
            set("expireDateToken", Timestamp.valueOf(exp));
        }
    }
}