package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("faculties")
public class Faculty extends Model {

    public String getName() { return getString("name"); }
    public void setName(String name) { set("name", name); }
}
