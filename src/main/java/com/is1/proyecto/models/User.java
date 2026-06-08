package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("users") // Esta anotación asocia explícitamente el modelo 'User' con la tabla 'users' en
                // la DB.
public class User extends Model {

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getPassword() {
        return getString("password");
    }

    public void setPassword(String password) {
        set("password", password);
    }

    public Role getRole() {
        String role = getString("role");
        if (role == null) {
            return null;
        }
        try {
            return Role.valueOf(role.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setRole(Role role) {
        if (role != null) {
            set("role", role.name());
        } else {
            set("role", null);
        }
    }

}