package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("examTables")
public class ExamTable extends Model {

    public boolean isOpen() {
        return "OPEN".equals(getString("status"));
    }

    public boolean isClosed() {
        return "CLOSED".equals(getString("status"));
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(getString("status"));
    }

    public void close() {
        set("status", "CLOSED").saveIt();
    }

    public void cancel() {
        set("status", "CANCELLED").saveIt();
    }
}
